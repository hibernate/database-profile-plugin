/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * @author Steve Ebersole
 */
public class PropertiesOnlyProfile extends AbstractProfileImpl {
	public PropertiesOnlyProfile(File propsFile, Project project) {
		super(
				propsFile.getParentFile().getParentFile(),
				project
		);

	}

	@Override
	public Configuration getTestingRuntimeConfiguration() {
		return null;
	}
}
