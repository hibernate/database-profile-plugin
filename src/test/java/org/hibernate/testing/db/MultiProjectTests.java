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
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				},
				"test"
		);
	}

	private void withAllProjects(Consumer<GradleRunner> gradleRunnerConsumer, String... args) {
		gradleRunnerConsumer.accept( createSub2GradleRunner( args ) );
		gradleRunnerConsumer.accept( createSubGradleRunner( args ) );
		gradleRunnerConsumer.accept( createRootGradleRunner( args ) );
	}

	private GradleRunner createRootGradleRunner(String... args) {
		return TestHelper.createGradleRunner( PROJECT_NAME, args );
	}

	private GradleRunner createSubGradleRunner(String... args) {
		return TestHelper.createGradleRunner( SUB_PROJECT_NAME, args );
	}

	private GradleRunner createSub2GradleRunner(String... args) {
		return TestHelper.createGradleRunner( SUB2_PROJECT_NAME, args );
	}

	@Test
	public void testExplicitDefaultProfile() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.build();
					System.out.println( buildResult.getOutput() );
				},
				"test",
				"-Pdb_profile_name=h2"
		);
	}

	@Test
	public void testProjectPropertyUsage() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				},
				"test",
				"-Pdb_profile_name=derby"
		);
	}

	@Test
	public void testProfileTask() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				},
				"test_derby"
		);
	}

	@Test
	public void testAllProfiles() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				},
				"testAllDbProfiles"
		);
	}

	@Test
	public void testAugmentProperties() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.build();

					System.out.println( buildResult.getOutput() );
				},
				"augmentTestProperties",
				"-Pdb_profile_name=derby"
		);
	}

	@Test
	public void testInvalidProjectProperty() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.buildAndFail();
					System.out.println( buildResult.getOutput() );
				},
				"test",
				"-Pdb_profile_name=mongodb"
		);
	}

	@Test
	public void testInvalidProfileTask() {
		withAllProjects(
				gradleRunner -> {
					final BuildResult buildResult = gradleRunner.buildAndFail();
					System.out.println( buildResult.getOutput() );
				},
				"test_monogdb"
		);
	}
}
