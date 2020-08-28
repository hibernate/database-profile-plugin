/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.io.File;
import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Steve Ebersole
 */
public class TestPropertiesAugmentTask extends AbstractTask {
	private final Profile selectedProfile;
	private final Project project;

	@Inject
	public TestPropertiesAugmentTask(
			Profile selectedProfile,
			Project project) {
		this.selectedProfile = selectedProfile;
		this.project = project;
	}

	@TaskAction
	public void augmentPropertiesFile() {
		final Task testResourcesTask = project.getTasks().findByName( "processTestResources" );
		final File testResourcesOutDir = ( (Copy) testResourcesTask ).getDestinationDir();
		final File hibernatePropertiesFile = new File( testResourcesOutDir, "hibernate.properties" );

		Helper.augmentHibernatePropertiesFile( hibernatePropertiesFile, selectedProfile, project );
	}
}
