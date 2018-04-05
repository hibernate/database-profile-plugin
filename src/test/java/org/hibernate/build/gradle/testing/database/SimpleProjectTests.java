/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.util.Properties;

import org.gradle.api.ProjectConfigurationException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.hibernate.build.gradle.testing.BuildException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Steve Ebersole
 */
public class SimpleProjectTests {

	private static final String PROJECT_NAME = "simple-project";

	@Test
	public void testValidProfile() {
		final BuildResult buildResult = GradleRunner.create()
				.withProjectDir( Helper.projectDirectory( PROJECT_NAME ) )
				.withArguments(
						"processTestResources",
						"-Pdb=h2",
						"--stacktrace",
						"--refresh-dependencies",
						"--no-build-cache"
				)
				.withDebug( true ) //Useful to debug the plugin code directly in the IDE
				.withPluginClasspath()
				.build();

		System.out.println( buildResult.getOutput() );

		final Properties writtenProperties = Helper.projectTestProperties( PROJECT_NAME );
		assertTrue( writtenProperties.containsKey( Helper.DIALECT_PROP_KEY ) );
		assertEquals( "H2Dialect", writtenProperties.get( Helper.DIALECT_PROP_KEY ) );
	}

	@Test
	public void testInvalidProfile() {
		try {
			GradleRunner.create()
					.withProjectDir( Helper.projectDirectory( PROJECT_NAME ) )
					.withArguments(
							"processTestResources",
							"-Pdb=mongodb",
							"--stacktrace",
							"--refresh-dependencies",
							"--no-build-cache"
					)
					.withDebug( true ) //Useful to debug the plugin code directly in the IDE
					.withPluginClasspath()
					.buildAndFail();
		}
		catch (Exception e) {
			assertTrue(
					e instanceof BuildException
							|| e.getCause() instanceof BuildException
			);
		}
	}
}
