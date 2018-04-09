/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import org.hibernate.build.gradle.testing.BuildException;

import groovy.lang.Closure;
import org.apache.tools.ant.filters.ReplaceTokens;

import static org.hibernate.build.gradle.testing.database.ProfilePlugin.PROFILE_PROVIDER_EXT_KEY;

/**
 * Used as a "grouping task" for performing "tasks" related to applying
 * a database profile.
 *
 * This task has no real "task action" - it merely
 * acts as a grouping for other tasks that rely on resolved Profile
 *
 * Also centralizes the resolution of the Profile
 *
 * @author Steve Ebersole
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ProfileTask extends AbstractTask {

	private final ProfileResolver profileResolver;

	public ProfileTask() {
		profileResolver = (ProfileResolver) getProject().getExtensions()
				.getExtraProperties()
				.get( PROFILE_PROVIDER_EXT_KEY );

		doLast(
				task -> {
					final Task testResourcesTask = getProject().getTasks().findByName( "processTestResources" );
					if ( testResourcesTask != null ) {
						final File testResourcesOutDir = ( (Copy) testResourcesTask ).getDestinationDir();
						final File hibernatePropertiesFile = new File(
								testResourcesOutDir,
								"hibernate.properties"
						);
						if ( hibernatePropertiesFile.exists() ) {
							overWriteProperties( hibernatePropertiesFile );
						}
					}
				}
		);
	}

	@TaskAction
	public void resolveProfile() {
		// no real task action itself - mainly used to attach actions to
		profileResolver.getSelectedProfile();
	}

	public void augment(Object propertiesFile) {
		augment( getProject().file( propertiesFile ) );
	}

	public void augment(File propertiesFile) {
		if ( profileResolver == null ) {
			return;
		}

		doLast( task -> overWriteProperties( propertiesFile ) );
	}

	private void overWriteProperties(File propertiesFile) {
		final Profile profile = profileResolver.getSelectedProfile();
		if ( profile == null ) {
			// do nothing
			return;
		}

		final Properties props = loadTestingProps( propertiesFile );

		boolean changed = false;
		for ( Map.Entry<String, Object> entry : profile.getHibernateProperties().entrySet() ) {
			final Object existing = props.put( entry.getKey(), entry.getValue() );
			changed = changed || existing != entry.getValue();
		}

		if ( changed ) {
			writeToBuildOutput( props, propertiesFile, profile );
		}
	}

	private static Properties loadTestingProps(File propsFile) {
		if ( ! propsFile.exists() ) {
			throw new BuildException( "Properties file to augment did not exist : " + propsFile.getAbsolutePath() );
		}

		if ( ! propsFile.isFile() ) {
			throw new BuildException( "java.io.File passed to augment did not refer to a file : " + propsFile.getAbsolutePath() );
		}

		final Properties testingProps = new Properties();

		if ( propsFile.exists() ) {
			try ( FileInputStream stream = new FileInputStream( propsFile ) ) {
				testingProps.load( stream );
			}
			catch (IOException e) {
				throw new BuildException( "Could not load properties file" );
			}
		}
		return testingProps;
	}

	private static void writeToBuildOutput(
			Properties testingProps,
			File propsFile,
			Profile profile) {
		try ( FileOutputStream stream = new FileOutputStream( propsFile ) ) {
			testingProps.store(
					stream,
					"Augmented for database profile `" + profile.getName() +
							"` - " + new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() )
			);
			stream.flush();
		}
		catch (IOException e) {
			throw new BuildException( "Unable to store augmented `hibernate.properties` testing properties file" );
		}
	}


	public void filterCopy(Closure<CopySpec> config) {
		final Profile profile = profileResolver.getSelectedProfile();
		if ( profile == null ) {
			// do nothing
			return;
		}

		doLast(
				task -> getProject().copy(
						copySpec -> {
							ConfigureUtil.configure( config, copySpec );
							copySpec.filter( profile.getHibernateProperties(), ReplaceTokens.class );
						}
				)
		);
	}

	public void extend(Action<Profile> action) {
		final Profile profile = profileResolver.getSelectedProfile();
		if ( profile == null ) {
			// do nothing
			return;
		}

		doLast(
				task -> action.execute( profile )
		);
	}
}
