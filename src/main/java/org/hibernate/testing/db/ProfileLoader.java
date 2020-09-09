/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.io.File;
import java.util.Properties;

import org.gradle.api.Project;

/**
 * Contract for resolving database profiles available via searching specified
 * directories
 *
 * @see DslExtension#getProfileSearchDirectories()
 *
 * @author Steve Ebersole
 */
class ProfileLoader {
	public static void loadProfiles(
			ProfileCreator profileCreator,
			DslExtension dslExtension,
			Project project) {
		final ProfileFragmentTarget fragmentTarget = new ProfileFragmentTarget( dslExtension, profileCreator );
		dslExtension.visitProfileSearchDirectories(
				directory -> applyProfilesFromDirectory(
						directory,
						fragmentTarget,
						profileCreator,
						dslExtension,
						project
				)
		);
	}

	private static void applyProfilesFromDirectory(
			File directory,
			ProfileFragmentTarget fragmentTarget,
			ProfileCreator profileCreator,
			DslExtension dslExtension,
			Project project) {
		final File[] paths = directory.listFiles();
		if ( paths == null ) {
			return;
		}

		for ( int i = 0; i < paths.length; i++ ) {
			final File path = paths[ i ];

			if ( ! path.exists() ) {
				continue;
			}

			if ( path.isFile() ) {
				final String pathName = path.getName();
				if ( pathName.endsWith( ".profile" ) || pathName.endsWith( ".gradle" ) ) {
					project.apply( Helper.asMap( "from", path, "to", fragmentTarget ) );
					continue;
				}

				// path was a file, no need to check any further
				continue;
			}

			if ( path.isDirectory() ) {
				// see if the directory is defines a "directory-based profile"...
				final Profile directoryBasedProfile = resolveDirectoryBasedProfile( path, profileCreator, dslExtension, project );
				if ( directoryBasedProfile != null ) {
					dslExtension.getProfiles().add( directoryBasedProfile );
					continue;
				}

				applyProfilesFromDirectory( path, fragmentTarget, profileCreator, dslExtension, project );
			}
		}
	}

	private static Profile resolveDirectoryBasedProfile(
			File directory,
			ProfileCreator profileCreator,
			DslExtension dslExtension,
			Project project) {
		final File propertiesFile = new File( directory, "hibernate.properties" );

		// bare minimum the profile needs to have a properties file
		final boolean hasPropertiesFile = propertiesFile.exists() && propertiesFile.isFile();
		if ( !hasPropertiesFile ) {
			return null;
		}

		final Profile profile = dslExtension.getProfiles().maybeCreate( directory.getName() );

		final Properties properties = Helper.loadProperties( propertiesFile );
		profile.applyHibernateProperties( properties );

		// optionally (though generally) the profile can also specify some jars
		project.getDependencies().add(
				profile.getDependencies().getName(),
				project.fileTree(
						directory,
						files -> files.exclude( "hibernate.properties" )
				)
		);

		return profile;
	}
}
