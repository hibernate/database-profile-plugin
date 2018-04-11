/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.util.List;
import java.util.Properties;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Steve Ebersole
 */
public class SimpleProjectTests {

	private static final String PROJECT_NAME = "simple-project";

	@Test
	public void testValidProfile() {
		final BuildResult buildResult = GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( PROJECT_NAME ) )
				.withArguments(
						"clean",
						"processTestResources",
						"-Pdatabase_profile_name=h2",
						"-P" + ProfileResolver.CUSTOM_DATABASES_DIRECTORY_KEY + "=../databases",
						"--stacktrace",
						"--refresh-dependencies",
						"--no-build-cache"
				)
				.withDebug( true )
				.withPluginClasspath()
				.build();

		System.out.println( buildResult.getOutput() );

		final Properties writtenProperties = TestHelper.projectTestProperties( PROJECT_NAME );
		assertTrue( writtenProperties.containsKey( TestHelper.DIALECT_PROP_KEY ) );
		assertEquals( "H2Dialect", writtenProperties.get( TestHelper.DIALECT_PROP_KEY ) );
	}

	@Test
	public void testAliasTask() {
		final BuildResult buildResult = GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( PROJECT_NAME ) )
				.withArguments(
						"clean",
						"applyDatabaseProfile_h2",
						"-P" + ProfileResolver.CUSTOM_DATABASES_DIRECTORY_KEY + "=../databases",
						"--stacktrace",
						"--refresh-dependencies",
						"--no-build-cache"
				)
				.withDebug( true )
				.withPluginClasspath()
				.build();

		System.out.println( buildResult.getOutput() );

		final Properties writtenProperties = TestHelper.projectTestProperties( PROJECT_NAME );
		assertTrue( writtenProperties.containsKey( TestHelper.DIALECT_PROP_KEY ) );
		assertEquals( "H2Dialect", writtenProperties.get( TestHelper.DIALECT_PROP_KEY ) );
	}

	@Test
	public void testInvalidProfile() {
		final GradleRunner runner = GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( PROJECT_NAME ) )
				.withArguments(
						"clean",
						"processTestResources",
						"-Pdatabase_profile_name=mongodb",
						"-P" + ProfileResolver.CUSTOM_DATABASES_DIRECTORY_KEY + "=../databases",
						"--stacktrace",
						"--refresh-dependencies",
						"--no-build-cache"
				)
				.withDebug( true )
				.withPluginClasspath();

		final BuildResult buildResult = runner.buildAndFail();
		System.out.println( buildResult.getOutput() );

		final List<BuildTask> failedTasks = buildResult.tasks( TaskOutcome.FAILED );
		assertEquals( 1, failedTasks.size() );
		assertEquals( ':' + ProfilePlugin.TASK_NAME, failedTasks.get( 0 ).getPath() );
	}
}
