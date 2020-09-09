/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import static org.hibernate.testing.db.Helper.CUSTOM_DATABASES_DIRECTORY_KEY;
import static org.hibernate.testing.db.Helper.STANDARD_DATABASES_DIRECTORY;

/**
 * Bean representing the user's configuration of the plugin
 *
 * @author Steve Ebersole
 */
public class DslExtension extends AbstractActionContainer implements ProfileAccess {
	private final Project project;

	/**
	 * Hibernate tests against H2 database by default
	 */
	private String defaultProfile = "h2";

	private final NamedDomainObjectContainer<Profile> profiles;

	private final List<File> profileSearchDirectories = new ArrayList<>();

	public DslExtension(ProfileCreator profileCreator, Project project) {
		this.project = project;

		this.profiles = project.container( Profile.class, profileCreator );

		final File databasesDirectory = project.file( STANDARD_DATABASES_DIRECTORY );
		if ( databasesDirectory.exists() && databasesDirectory.isDirectory() ) {
			profileSearchDirectories.add( databasesDirectory );
		}

		final File customDirectoryViaProperty = resolveCustomDirectoryProperty( project );
		if ( customDirectoryViaProperty != null
				&& customDirectoryViaProperty.exists()
				&& customDirectoryViaProperty.isDirectory() ) {
			profileSearchDirectories.add( customDirectoryViaProperty );
		}

		project.afterEvaluate(
				p -> {
					// just want to get a callback for debugging
					project.getLogger().lifecycle(
							"After evaluation, discovered profiles include:"
					);

					profiles.forEach(
							profile -> project.getLogger().lifecycle( "   > {}", profile.getName() )
					);
				}
		);
	}

	private static File resolveCustomDirectoryProperty(Project project) {
		Object localDirectoryProperty = System.getProperty( CUSTOM_DATABASES_DIRECTORY_KEY );

		if ( project.hasProperty( CUSTOM_DATABASES_DIRECTORY_KEY ) ) {
			localDirectoryProperty = project.property( CUSTOM_DATABASES_DIRECTORY_KEY );
		}

		if ( localDirectoryProperty != null ) {
			return project.file( localDirectoryProperty );
		}

		return null;
	}

	public String getDefaultProfile() {
		return defaultProfile;
	}

	public void setDefaultProfile(String defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	public NamedDomainObjectContainer<Profile> getProfiles() {
		return profiles;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Profile search directories

	/**
	 * Specifies directories which will be searched for profile definitions
	 *
	 * @see ProfileLoader
	 */
	public List<File> getProfileSearchDirectories() {
		return profileSearchDirectories;
	}

	public void setProfileSearchDirectories(List<File> profileSearchDirectories) {
		this.profileSearchDirectories.clear();
		this.profileSearchDirectories.addAll( profileSearchDirectories );
	}

	public void profileSearchDirectory(Object directoryReference) {
		final File directory = project.file( directoryReference );

		if ( ! directory.exists() ) {
			throw new IllegalArgumentException( "Specified directory does not exist : " + directory.getAbsolutePath() );
		}

		if ( ! directory.isDirectory() ) {
			throw new IllegalArgumentException( "Specified directory is not a directory : " + directory.getAbsolutePath() );
		}

		profileSearchDirectories.add( project.file( directoryReference ) );
	}

	void visitProfileSearchDirectories(Consumer<File> additionalProfileDirectoryConsumer) {
		profileSearchDirectories.forEach( additionalProfileDirectoryConsumer );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ProfileAccess

	@Override
	public Profile findProfile(String name) {
		return profiles.findByName( name );
	}

	@Override
	public Profile getProfile(String name) throws UnknownProfileException {
		return profiles.getByName( name );
	}

	@Override
	public void visitProfiles(Consumer<Profile> profileConsumer) {
		profiles.forEach( profileConsumer );
	}

	@Override
	public void visitProfileNames(Consumer<String> profileNameConsumer) {
		profiles.forEach( profile -> profileNameConsumer.accept( profile.getName() ) );
	}
}
