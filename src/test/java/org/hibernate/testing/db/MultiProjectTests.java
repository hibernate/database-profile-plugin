/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.util.function.Consumer;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.junit.Test;

/**
 * @author Steve Ebersole
 */
public class MultiProjectTests {

	private static final String PROJECT_NAME = "multi";
	private static final String SUB_PROJECT_NAME = "multi/sub";
	private static final String SUB2_PROJECT_NAME = "multi/sub2";

	@Test
	public void testImplicitProfile() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.withArguments( "test" ).build();

					System.out.println( buildResult.getOutput() );
				}
		);
	}

	private void withAllProjects(Consumer<GradleRunner> gradleRunnerConsumer) {
		gradleRunnerConsumer.accept( createSub2GradleRunner() );
		gradleRunnerConsumer.accept( createSubGradleRunner() );
		gradleRunnerConsumer.accept( createRootGradleRunner() );
	}

	private GradleRunner createRootGradleRunner() {
		return GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( PROJECT_NAME ) )
				.withDebug( true )
				.withPluginClasspath();
	}

	private GradleRunner createSubGradleRunner() {
		return GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( SUB_PROJECT_NAME ) )
				.withDebug( true )
				.withPluginClasspath();
	}

	private GradleRunner createSub2GradleRunner() {
		return GradleRunner.create()
				.withProjectDir( TestHelper.projectDirectory( SUB2_PROJECT_NAME ) )
				.withDebug( true )
				.withPluginClasspath();
	}

	@Test
	public void testExplicitDefaultProfile() {
		withAllProjects(
				gradleRunner -> {
					final GradleRunner runner = gradleRunner.withArguments(
							"test",
							"-Pdb_profile_name=h2"
					);
					final BuildResult buildResult = runner.build();
					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testProjectPropertyUsage() {
		withAllProjects(
				baseRunner -> {
					final GradleRunner gradleRunner = baseRunner.withArguments(
							"test",
							"-Pdb_profile_name=derby"
					);
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testProfileTask() {
		withAllProjects(
				gradleRunnerBase -> {
					final GradleRunner gradleRunner = gradleRunnerBase.withArguments( "test_derby" );

					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testAllProfiles() {
		withAllProjects(
				gradleRunnerBase -> {
					final GradleRunner gradleRunner = gradleRunnerBase.withArguments( "testAllDbProfiles" );

					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testAugmentProperties() {
		withAllProjects(
				gradleRunnerBase -> {
					final GradleRunner gradleRunner = gradleRunnerBase.withArguments(
							"augmentTestProperties",
							"-Pdb_profile_name=derby"
					);

					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testInvalidProjectProperty() {
		withAllProjects(
				gradleRunnerBase -> {
					final GradleRunner gradleRunner = gradleRunnerBase.withArguments(
							"test",
							"-Pdb_profile_name=mongodb"
					);

					final BuildResult buildResult = gradleRunner.buildAndFail();
					System.out.println( buildResult.getOutput() );
				}
		);
	}

	@Test
	public void testInvalidProfileTask() {
		withAllProjects(
				gradleRunnerBase -> {
					final GradleRunner gradleRunner = gradleRunnerBase.withArguments( "test_monogdb" );

					final BuildResult buildResult = gradleRunner.buildAndFail();
					System.out.println( buildResult.getOutput() );
				}
		);
	}
}
