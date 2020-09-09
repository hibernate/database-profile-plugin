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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.testing.Test;

import org.hibernate.testing.db.alloc.DatabaseAllocator;

import static org.hibernate.testing.db.Helper.DSL_NAME;
import static org.hibernate.testing.db.Helper.LEGACY_PROFILE_NAME_CONFIG_NAME;
import static org.hibernate.testing.db.Helper.PROFILE_NAME_CONFIG_NAME;
import static org.hibernate.testing.db.Helper.TEST_ALL_PROFILES_TASK_NAME;

/**
 * Manages applying resources associated with a named database profile
 *
 * @author Steve Ebersole
 */
public class ProfilePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final ProfileCreator profileCreator = new ProfileCreator( project );

		final DslExtension dslExtension = project.getExtensions().create(
				DSL_NAME,
				DslExtension.class,
				profileCreator,
				project
		);

		if ( project.hasProperty( PROFILE_NAME_CONFIG_NAME ) ) {
			final Object property = project.property( PROFILE_NAME_CONFIG_NAME );
			if ( property != null ) {
				dslExtension.setDefaultProfile( property.toString() );
			}
		}
		else if ( project.hasProperty( LEGACY_PROFILE_NAME_CONFIG_NAME ) ) {
			final Object property = project.property( LEGACY_PROFILE_NAME_CONFIG_NAME );
			if ( property != null ) {
				dslExtension.setDefaultProfile( property.toString() );
			}
		}

		AfterEvalAction.apply( profileCreator, dslExtension, project );
	}

	private static class AfterEvalAction {

		public static void apply(
				ProfileCreator profileCreator,
				DslExtension dslExtension,
				Project project) {
			final AfterEvalAction afterEvalAction = new AfterEvalAction( profileCreator, dslExtension, project );
			afterEvalAction.visitProject( project );

			project.getTasks().create( "showProfileTestInfo", ShowTestTaskInfo.class );
		}

		private final ProfileCreator profileCreator;
		private final DslExtension dslExtension;
		private final Project project;

		private final List<Project> allProjects = new ArrayList<>();

		public AfterEvalAction(
				ProfileCreator profileCreator,
				DslExtension dslExtension,
				Project project) {
			this.profileCreator = profileCreator;
			this.dslExtension = dslExtension;
			this.project = project;
		}

		private void visitProject(Project project) {
			allProjects.add( project );
			project.afterEvaluate( this::projectEvaluated );
			project.getSubprojects().forEach( this::visitProject );
		}

		private void projectEvaluated(Project project) {
			allProjects.remove( project );

			if ( allProjects.isEmpty() ) {
				ProfilePlugin.applyProfiles( profileCreator, dslExtension, this.project );
			}
		}
	}

	private static void applyProfiles(
			ProfileCreator profileCreator,
			DslExtension dslExtension,
			Project project) {
		ProfileLoader.loadProfiles( profileCreator, dslExtension, project );
		DatabaseAllocator.apply( dslExtension, project );

		final JavaPluginConvention javaPluginConvention = project.getConvention().findPlugin( JavaPluginConvention.class );

		if ( javaPluginConvention != null ) {
			applyToJavaProject(
					javaPluginConvention,
					dslExtension,
					project
			);
		}
		else {
			applyToNonJavaProject( dslExtension, project );
		}
	}

	private static void applyProfilesToProject(Project project, DslExtension dslExtension) {
		final JavaPluginConvention javaPluginConvention = project.getConvention().findPlugin( JavaPluginConvention.class );

		if ( javaPluginConvention != null ) {
			applyToJavaProject(
					javaPluginConvention,
					dslExtension,
					project
			);
		}
		else {
			applyToNonJavaProject( dslExtension, project );
		}
	}

	private static void applyToJavaProject(
			JavaPluginConvention javaPluginConvention,
			DslExtension dslExtension,
			Project project) {
		// create the grouping task for running tests against all profiles
		final Task groupingTask = project.getTasks().create( TEST_ALL_PROFILES_TASK_NAME );
		groupingTask.setGroup( "database" );
		groupingTask.setDescription( "Runs tests against all discovered database profiles" );

		// find the test task...
		final Test mainTestTask = (Test) project.getTasks().findByPath( "test" );
		if ( mainTestTask == null ) {
			project.getLogger().debug( "No test task found, skipping db-profile application" );
			return;
		}

		dslExtension.getProfiles().forEach(
				(profile) -> {
					final Test profileTestTask = Helper.makeCopy( mainTestTask, javaPluginConvention, profile, dslExtension, project );
					Helper.applyProfile( profile, profileTestTask, dslExtension, project );
					groupingTask.dependsOn( profileTestTask );
				}
		);

		final Profile selectedProfile = dslExtension.getProfiles().getByName( dslExtension.getDefaultProfile() );
		Helper.applyProfile( selectedProfile, mainTestTask, dslExtension, project );

		final Task testResourcesTask = project.getTasks().getByName( "processTestResources" );

		testResourcesTask.doLast(
				task -> {
					final File testResourcesOutDir = ( (Copy) testResourcesTask ).getDestinationDir();
					final File hibernatePropertiesFile = new File( testResourcesOutDir, "hibernate.properties" );

					Helper.augmentHibernatePropertiesFile( hibernatePropertiesFile, selectedProfile, project );
				}
		);

		final TestPropertiesAugmentTask augmentTestPropertiesTask = project.getTasks().create(
				TestPropertiesAugmentTask.NAME,
				TestPropertiesAugmentTask.class,
				selectedProfile,
				project
		);
		augmentTestPropertiesTask.setGroup( "database" );
	}

	private static void applyToNonJavaProject(DslExtension dslExtension, Project project) {
		if ( project.getSubprojects().isEmpty() ) {
			return;
		}

		project.getSubprojects().forEach(
				subProject -> applyProfilesToProject( subProject, dslExtension )
		);
	}

}
