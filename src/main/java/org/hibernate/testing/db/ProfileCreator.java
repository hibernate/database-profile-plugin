/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db;

import java.util.Locale;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * @author Steve Ebersole
 */
public class ProfileCreator implements NamedDomainObjectFactory<Profile> {
	private final Project project;

	public ProfileCreator(Project project) {
		this.project = project;
	}

	@Override
	public Profile create(String name) {
		final String dependenciesName = "profileDependencies"
				+ name.substring( 0, 1 ).toUpperCase( Locale.ROOT )
				+ name.substring( 1 );
		final Configuration profileDependencies = project.getConfigurations().maybeCreate( dependenciesName );

		return new Profile( name, profileDependencies, project );
	}
}
