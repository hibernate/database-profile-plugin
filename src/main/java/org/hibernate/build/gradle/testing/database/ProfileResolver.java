/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.util.concurrent.Callable;

import org.gradle.api.Project;

import org.hibernate.build.gradle.testing.BuildException;

import static org.hibernate.build.gradle.testing.database.ProfilePlugin.JDBC_DIR;
import static org.hibernate.build.gradle.testing.database.ProfilePlugin.MATRIX_BUILD_FILE;

/**
 * @author Steve Ebersole
 */
public class ProfileResolver implements Callable<Profile> {
	private final Project project;
	private final ProfileExtension extension;

	private boolean resolved;
	private Profile resolvedProfile;

	public ProfileResolver(Project project) {
		this.project = project;
		this.extension = project.getExtensions().getByType( ProfileExtension.class );
	}

	@Override
	public Profile call() {
		if ( !resolved ) {
			this.resolvedProfile = resolveProfileToUse( project, extension );
			this.resolved = true;
		}

		return resolvedProfile;
	}

	public static Profile resolveProfileToUse(Project project, ProfileExtension extension) {
		if ( extension.getProfileToUse().getOrNull() == null ) {
			// should never happen, but be sure
			return null;
		}

		// highest precedence - try to find a profile local to this project first
		final Profile localProfile = resolveLocalProfile( project, extension );
		if ( localProfile != null ) {
			return localProfile;
		}

		// next - custom profiles (from search directories)
		final Profile customProfile = resolveCustomProfile( project, extension );
		if ( customProfile != null ) {
			return customProfile;
		}

		// lastly - see if it is resolvable from any parents
		final Profile profileFromParent = findSelectedProfileInParents( project, extension );
		if ( profileFromParent != null ) {
			return profileFromParent;
		}

		throw new BuildException( "Could not resolve specified database profile - " + extension.getProfileToUse() );
	}

	/**
	 * Look for the profile in the standard `databases/` search directory local to this project
	 */
	private static Profile resolveLocalProfile(Project project, ProfileExtension extension) {
		final File localDatabasesDir = project.file( ProfilePlugin.STANDARD_DATABASES_DIRECTORY );

		final Profile databaseProfile = searchDirectory( localDatabasesDir, project, extension );
		if ( databaseProfile != null ) {
			return databaseProfile;
		}

		return null;
	}

	/**
	 * Look for the profile in the standard `databases/` search directory local to this project
	 */
	private static Profile resolveCustomProfile(Project project, ProfileExtension extension) {
		for ( File file : extension.getCustomSearchDirectories() ) {
			final Profile profile = searchDirectory( file, project, extension );
			if ( profile != null ) {
				return profile;
			}
		}

		return null;
	}

	private static Profile searchDirectory(File dir, Project project, ProfileExtension extension) {
		final boolean canSearch = dir.exists() && dir.isDirectory();
		if ( ! canSearch ) {
			return null;
		}

		if ( extension.getProfileToUse().get().equals( dir.getName() ) ) {
			final Profile profile = findDatabaseProfile( dir, project );

			if ( profile != null ) {
				return profile;
			}
		}

		//noinspection ConstantConditions
		for ( File subDir : dir.listFiles() ) {
			final Profile profile = searchDirectory( subDir, project, extension );

			if ( profile != null ) {
				return profile;
			}
		}

		return null;
	}

	private static Profile findDatabaseProfile(File directory, Project project) {
		final File matrixDotGradleFile = new File( directory, MATRIX_BUILD_FILE );
		if ( matrixDotGradleFile.exists() && matrixDotGradleFile.isFile() ) {
			project.getLogger().debug( "Found matrix.gradle file : " + matrixDotGradleFile );
			return new MatrixDotGradleProfile( matrixDotGradleFile, project );
		}

		final File jdbcDirectory = new File( directory, JDBC_DIR );
		if ( jdbcDirectory.exists() && jdbcDirectory.isDirectory() ) {
			return new JdbcDirectoryProfile( jdbcDirectory, project );
		}

		return null;
	}

	private static Profile findSelectedProfileInParents(Project project, ProfileExtension extension) {
		final Project parent = project.getParent();

		if ( parent == null ) {
			return null;
		}

		final Profile databaseProfile = resolveLocalProfile( parent, extension );
		if ( databaseProfile != null ) {
			return databaseProfile;
		}

		return findSelectedProfileInParents( parent, extension );
	}
}
