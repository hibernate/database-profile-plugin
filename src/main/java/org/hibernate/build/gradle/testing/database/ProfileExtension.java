/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;

import static org.hibernate.build.gradle.testing.database.ProfilePlugin.CUSTOM_DATABASES_DIRECTORY_KEY;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProfileExtension {
	private final Project project;

	private Set<File> customSearchDirectories;
	private Property<String> profileName;

	public ProfileExtension(Project project) {
		this.project = project;
		this.customSearchDirectories = defaultSearchDirectories( project );
	}

	private static Set<File> defaultSearchDirectories(Project project) {
		final HashSet<File> directories = new HashSet<>();

		final File localDirectory = resolveLocalDirectory( project );
		if ( localDirectory != null ) {
			directories.add( localDirectory );
		}

		return directories;
	}

	private static File resolveLocalDirectory(Project project) {
		Object localDirectoryProperty = System.getProperty( CUSTOM_DATABASES_DIRECTORY_KEY );

		if ( project.hasProperty( CUSTOM_DATABASES_DIRECTORY_KEY ) ) {
			localDirectoryProperty = project.property( CUSTOM_DATABASES_DIRECTORY_KEY );
		}

		if ( localDirectoryProperty != null ) {
			return project.file( localDirectoryProperty );
		}

		return null;
	}

	@Input
	public Property<String> getProfileToUse() {
		return profileName;
	}

	public void setProfileToUse(String profileName) {
		this.profileName.set( profileName );
	}

	@InputFiles
	public Set<File> getCustomSearchDirectories() {
		return customSearchDirectories;
	}

	public void setCustomSearchDirectories(Set<File> customSearchDirectories) {
		this.customSearchDirectories = customSearchDirectories;
	}

	public void customSearchDirectory(File directory) {
		if ( customSearchDirectories == null ) {
			customSearchDirectories = new HashSet<>();
		}
		customSearchDirectories.add( directory );
	}

	public void customSearchDirectory(Object directory) {
		customSearchDirectory( project.file( directory ) );
	}
}
