/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.testing.Test;

/**
 * @author Steve Ebersole
 */
public class ShowTestTaskInfo extends DefaultTask {
	@TaskAction
	public void showInfo()  {
		final String profileFilter = getProject().hasProperty( "db" )
				? (String) getProject().property( "db" )
				: null;

		getProject().getTasks().forEach(
				task -> {
					if ( task instanceof Test && !":test".equals( task.getPath() ) ) {
						final Test testTask = (Test) task;

						final int taskProfileDelimiterPosition = task.getName().indexOf( "_" );
						if ( taskProfileDelimiterPosition > 1 ) {
							final String taskProfileName = task.getName().substring( taskProfileDelimiterPosition + 1 );

							boolean output = true;

							if ( profileFilter != null ) {
								if ( ! taskProfileName.equals( profileFilter ) ) {
									output = false;
								}
							}

							if ( output ) {
								renderTaskInfo( testTask, getLogger(), getProject() );
							}
						}
					}
				}
		);
	}

	public static void renderTaskInfo(Test testTask, Logger logger, Project project) {
		logger.lifecycle( "########################################################" );
		logger.lifecycle( "Information for `{}` profile Test task", testTask.getPath() );
		logger.lifecycle( "########################################################" );

		logger.lifecycle( "  > Classpath:" );
		testTask.getClasspath().forEach(
				(classpathElement) -> {
					logger.lifecycle( "    > {}", classpathElement.getPath() );
				}
		);

		logger.lifecycle( "  > Includes:" );
		testTask.getIncludes().forEach(
				(include) -> {
					logger.lifecycle( "    > {}", include );
				}
		);
	}
}
