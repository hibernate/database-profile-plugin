/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */

package org.hibernate.build.gradle.testing.database;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

/**
 * Plugin used to apply notion of database profiles
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
@SuppressWarnings("WeakerAccess")
public class ProfilePlugin implements Plugin<Project> {

	public static final String TASK_NAME = "applyDatabaseProfile";
	public static final String PROFILE_PROVIDER_EXT_KEY = ProfilePlugin.class.getName() + ":profileProvider";

	private ProfileResolver profileResolver;

	ProfileResolver getProfileResolver() {
		return profileResolver;
	}

	public void apply(Project project) {
		profileResolver = new ProfileResolver( project );
		project.getExtensions().getExtraProperties().set(
				PROFILE_PROVIDER_EXT_KEY,
				profileResolver
		);

		final ProfileTask applyTask = project.getTasks().create( TASK_NAME, ProfileTask.class );

		final Task testTask = project.getTasks().findByName( "test" );
		final Copy resourcesTask = (Copy) project.getTasks().findByName( "processTestResources" );
		if ( resourcesTask != null ) {
			applyTask.dependsOn( resourcesTask );
			resourcesTask.finalizedBy( applyTask );
		}

		for ( String name : profileResolver.getAvailableProfileNames() ) {
			if ( testTask != null ) {
				// the project has a test task - create a profile-specific test "task"
				final Task profileTestTask = project.getTasks().create( "test_" + name );
				profileTestTask.getInputs().property( "profileName", name );
				profileTestTask.doFirst(
						task -> profileResolver.injectSelectedProfile( name )
				);

				// finalizedBy ensures that the "real" test task will always be run after
				// this profile-specific task
				profileTestTask.finalizedBy( testTask );
			}

			if ( resourcesTask != null ) {
				final Task profileResourcesTask = project.getTasks().create( "processTestResources_" + name );
				profileResourcesTask.getInputs().property( "profileName", name );
				profileResourcesTask.dependsOn( resourcesTask );
				profileResourcesTask.finalizedBy( applyTask );
				profileResourcesTask.doFirst(
						task -> {
							profileResolver.injectSelectedProfile( name );
						}
				);
			}
		}
    }
}
