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

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;

/**
 * Plugin used to apply notion of database profiles
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
@SuppressWarnings("WeakerAccess")
public class ProfilePlugin implements Plugin<Project> {
	public static final String EXTENSION_NAME = "databaseProfiles";

	/**
	 * The directory containing standard database profiles.
	 */
    public static final String STANDARD_DATABASES_DIRECTORY = "databases";

	/**
	 * Names a system setting key that can be set to point to a directory containing additional, custom
	 * database profiles.
	 */
	public static final String CUSTOM_DATABASES_DIRECTORY_KEY = "hibernate-matrix-databases";

	public static final String MATRIX_BUILD_FILE = "matrix.gradle";
	public static final String JDBC_DIR = "jdbc";

	private static final String TASK_NAME = "applyDatabaseProfile";

	public void apply(Project project) {
		/// Create the extension
        final ProfileExtension extension = project.getExtensions().create(
				EXTENSION_NAME,
				ProfileExtension.class,
				project
		);

		final ProfileTask groupingTask = project.getTasks().create( TASK_NAME, ProfileTask.class );

        project.afterEvaluate(
        		p -> {
        			if ( extension.getProfileToUse().getOrNull() != null ) {
        				applyResolver( project, groupingTask );
					}
				}
		);

    }

	private void applyResolver(Project project, ProfileTask groupingTask) {
		final Copy testResourcesTask = (Copy) project.getTasks().findByName( "processTestResources" );
		if ( testResourcesTask != null ) {
			final File hibPropsFile = new File( testResourcesTask.getDestinationDir(), "hibernate.properties" );
			if ( hibPropsFile.exists() ) {
				groupingTask.augment( hibPropsFile );
			}

			testResourcesTask.finalizedBy( TASK_NAME );
		}
	}
}
