/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Steve Ebersole
 */
public class SingleProjectTests {

	private static final String PROJECT_NAME = "single";

	private void validateTaskResult(BuildTask task) {
		assertThat( task, notNullValue() );

		assertThat( task.getOutcome(), not( TaskOutcome.FAILED ) );
	}

	private static GradleRunner createGradleRunner() {
		return TestHelper.createGradleRunner( PROJECT_NAME );
	}

	@Test
	public void testImplicitProfile() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments( "test" );
		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":test" ) );
	}

	@Test
	public void testProjectPropertyUsage() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments(
				"test",
				"-Pdb_profile_name=h2"
		);

		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":test" ) );
	}

	@Test
	public void testProfileTaskUsage() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments( "test_derby" );
		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":test_derby" ) );
	}

	@Test
	public void testCustomDirectoryUsage() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments(
				"test",
				"-Pdb_profile_name=custom",
				"-Pcustom_profiles_dir=../shared/profiles"
		);

		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":test" ) );

	}

	@Test
	public void testCustomDirectoryTaskUsage() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments(
				"test_custom",
				"-Pcustom_profiles_dir=../shared/profiles"
		);

		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":test_custom" ) );
	}

	@Test
	public void testAllProfiles() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments( "testAllDbProfiles" );

		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":testAllDbProfiles" ) );

	}

	@Test
	public void testAugmentProperties() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments(
				"augmentTestProperties",
				"-Pdb_profile_name=derby"
		);

		final BuildResult buildResult = gradleRunner.build();
		TestHelper.logRunnerOutput( buildResult );

		validateTaskResult( buildResult.task( ":augmentTestProperties" ) );
	}

	@Test
	public void testInvalidProjectProperty() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments(
				"test",
				"-Pdb_profile_name=mongodb"
		);

		final BuildResult buildResult = gradleRunner.buildAndFail();
		TestHelper.logRunnerOutput( buildResult );
	}

	@Test
	public void testInvalidProfileTask() {
		final GradleRunner gradleRunner = createGradleRunner().withArguments( "test_monogdb" );

		final BuildResult buildResult = gradleRunner.buildAndFail();
		TestHelper.logRunnerOutput( buildResult );
	}
}
