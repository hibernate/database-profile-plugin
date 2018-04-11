/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.Project;

import org.hibernate.build.gradle.testing.BuildException;
import org.hibernate.build.gradle.testing.Helper;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class ProfileResolver {
    public static final String STANDARD_DATABASES_DIRECTORY = "databases";
	public static final String CUSTOM_DATABASES_DIRECTORY_KEY = "custom_profiles_dir";
	public static final String PROFILE_PROP_NAME = "database_profile_name";
	public static final String LEGACY_PROFILE_PROP_NAME = "db";
	public static final String MATRIX_BUILD_FILE = "matrix.gradle";
	public static final String JDBC_DIR = "jdbc";

	private static final String STASH_PROFILE_NAME_KEY = "profileName";

	private final Project project;
	private final Map<String,ProfileSelector> profileSelectorByName;

	private Profile profile;
	private boolean resolved;

	ProfileResolver(Project project) {
		this.project = project;

		final Map<String,ProfileSelector> tmp = new HashMap<>();
		processParentProfiles( tmp );
		processCustomProfiles( tmp );
		processProjectProfiles( tmp );
		this.profileSelectorByName = Collections.unmodifiableMap( tmp );

		project.getLogger().debug(
				"Available database profiles : %s",
				String.join( ",", profileSelectorByName.keySet() )
		);
	}

	public Set<String> getAvailableProfileNames() {
		return profileSelectorByName.keySet();
	}

	public boolean isResolved() {
		return resolved;
	}

	public Profile getSelectedProfile() {
		if ( profile == null ) {
			// Contract for #get says that we cannot return null
			if ( ! resolved ) {
				// If our #profile reference has not been resolved,
				// resolve it
				profile = resolveProfile();
			}
		}

		return profile;
	}

	private Profile resolveProfile() {
		if ( resolved ) {
			return profile;
		}

		// no matter what happens from here, we are resolved
		resolved = true;

		// see if we had a previous "stash"
		final File stashFile = stashFile( project );
		String stashedProfileName = null;
		if ( stashFile.exists() ) {
			final Properties properties = Helper.loadProperties( stashFile );
			stashedProfileName = properties.getProperty( STASH_PROFILE_NAME_KEY );
		}

		final String selectedProfileName = determineProfileNameToUse( project, stashedProfileName );

		if ( ! selectedProfileName.equals( stashedProfileName ) ) {
			// the profile changed from the last run (stash)
			final Properties stashProperties = new Properties();
			stashProperties.setProperty( STASH_PROFILE_NAME_KEY, selectedProfileName );
			Helper.writeProperties(
					stashProperties,
					stashFile,
					"Database profile stash (" + selectedProfileName + ") - " + new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() )
			);
		}

		final ProfileSelector selector = profileSelectorByName.get( selectedProfileName );
		if ( selector == null ) {
			// a profile was requested, but we could not find it
			throw new BuildException(
					"Unable to resolve database profile - " + selectedProfileName +
							" [available : " + String.join( ",", profileSelectorByName.keySet() ) + "]"
			);
		}

		return selector.select();
	}

	private static File stashFile(Project project) {
		return project.file( new File( project.getBuildDir(), "profile-testing/stash.properties" ) );

	}

	private static String determineProfileNameToUse(Project project, String stashedProfileName) {
		if ( project.hasProperty( PROFILE_PROP_NAME ) ) {
			return (String) project.property( PROFILE_PROP_NAME );
		}

		final String sysProp = System.getProperty( PROFILE_PROP_NAME );
		if ( sysProp != null ) {
			return sysProp;
		}

		if ( project.hasProperty( LEGACY_PROFILE_PROP_NAME ) ) {
			return (String) project.property( LEGACY_PROFILE_PROP_NAME );
		}

		final String legacySysProp = System.getProperty( LEGACY_PROFILE_PROP_NAME );
		if ( legacySysProp != null ) {
			return legacySysProp;
		}

		return stashedProfileName != null ? stashedProfileName : "h2";
	}


	private Project getProject() {
		return project;
	}

	private void processParentProfiles(Map<String, ProfileSelector> map) {
		processParentProfiles( getProject(), map );
	}

	private void processParentProfiles(Project project, Map<String, ProfileSelector> map) {
		final Project parent = project.getParent();

		if ( parent == null ) {
			return;
		}

		// top-down
		processParentProfiles( parent, map );
		processProjectProfiles( parent, map );

	}

	private void processCustomProfiles(Map<String, ProfileSelector> map) {
		final File customDir = resolveCustomProfileDirectory( getProject() );
		if ( customDir != null ) {
			searchDirectory( customDir, map, getProject() );
		}
	}

	private static void searchDirectory(File dir, Map<String, ProfileSelector> map, Project project) {
		final boolean canSearch = dir.exists() && dir.isDirectory();
		if ( ! canSearch ) {
			return;
		}

		final ProfileSelector profileSelector = findDatabaseProfile( dir, project );
		if ( profileSelector != null ) {
			map.put( profileSelector.getProfileName(), profileSelector );
		}

		//noinspection ConstantConditions
		for ( File subDir : dir.listFiles() ) {
			searchDirectory( subDir, map, project );
		}
	}



	private static ProfileSelector findDatabaseProfile(File directory, Project project) {
		final File matrixDotGradleFile = new File( directory, MATRIX_BUILD_FILE );
		if ( matrixDotGradleFile.exists() && matrixDotGradleFile.isFile() ) {
			project.getLogger().debug( "Found matrix.gradle file : " + matrixDotGradleFile );
			return new ProfileSelector() {
				@Override
				public String getProfileName() {
					return directory.getName();
				}

				@Override
				public Profile select() {
					return new MatrixDotGradleProfile( matrixDotGradleFile, project );
				}
			};
		}

		final File jdbcDirectory = new File( directory, JDBC_DIR );
		if ( jdbcDirectory.exists() && jdbcDirectory.isDirectory() ) {
			return new ProfileSelector() {
				@Override
				public String getProfileName() {
					return directory.getName();
				}

				@Override
				public Profile select() {
					return new JdbcDirectoryProfile( jdbcDirectory, project );
				}
			};
		}

		return null;
	}


	private static File resolveCustomProfileDirectory(Project project) {
		Object localDirectoryProperty = System.getProperty( CUSTOM_DATABASES_DIRECTORY_KEY );

		if ( project.hasProperty( CUSTOM_DATABASES_DIRECTORY_KEY ) ) {
			localDirectoryProperty = project.property( CUSTOM_DATABASES_DIRECTORY_KEY );
		}

		if ( localDirectoryProperty != null ) {
			return project.file( localDirectoryProperty );
		}

		return null;
	}

	private void processProjectProfiles(Map<String, ProfileSelector> map) {
		processProjectProfiles( getProject(), map );
	}

	private void processProjectProfiles(Project project, Map<String, ProfileSelector> map) {
		searchDirectory(
				project.file( STANDARD_DATABASES_DIRECTORY ),
				map,
				project
		);
	}

	void injectSelectedProfile(Profile selectedProfile) {
		if ( this.profile != null ) {
			// error?  for now, just log it
			project.getLogger().debug( "replacing selected Profile : %s -> %s", profile, selectedProfile );
		}

		this.resolved = true;
		this.profile = selectedProfile;
	}

	void injectSelectedProfile(String name) {
		injectSelectedProfile( profileSelectorByName.get( name ).select() );
	}
}
