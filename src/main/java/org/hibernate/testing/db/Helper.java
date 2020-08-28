/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

/**
 * @author Steve Ebersole
 */
public class Helper {
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Constants

	/**
	 * Name of the DSL extension block
	 */
	public static final String DSL_NAME = "databases";

	/**
	 * Added to the Test task as an input property to add the selected profile as
	 * part of the test's up-to-date checking
	 */
	public static final String TEST_TASK_PROFILE_KEY = "db-profile";

	/**
	 * Used to pass the name of the database profile to use for testing as a project (-P) property
	 */
	public static final String PROFILE_NAME_CONFIG_NAME = "db_profile_name";

	/**
	 * Used to pass the name of the database profile to use for testing as a project (-P) property
	 */
	public static final String LEGACY_PROFILE_NAME_CONFIG_NAME = "db";

	/**
	 * Name of the standard directory, relative to a project's root, that is searched for profiles.
	 */
	public static final String STANDARD_DATABASES_DIRECTORY = "databases";

	public static final String CUSTOM_DATABASES_DIRECTORY_KEY = "custom_profiles_dir";

	/**
	 * A "grouping" task for executing tests against all resolved database profiles
	 */
	public static final String TEST_ALL_PROFILES_TASK_NAME = "testAllDbProfiles";

	public static final String PROFILE_GRADLE_FILE = "profile.gradle";

	/**
	 * Convenience method to determine the output directory for the specified profile name
	 */
	public static File determineOutputDirectory(Project project, String profileName) {
		// i.e. `${buildDir}/dbProfile/h2`

		return new File(
				new File( project.getBuildDir(), "dbProfile" ),
				profileName
		);
	}

	public static void augmentHibernatePropertiesFile(
			File hibernatePropertiesFile,
			Profile selectedProfile,
			Project project) {
		final Properties augmented;
		if ( hibernatePropertiesFile.exists() ) {
			augmented =  loadProperties( hibernatePropertiesFile );
		}
		else {
			augmented = new Properties();
		}

		boolean changed = false;
		for ( Map.Entry<String, Object> entry : selectedProfile.getHibernateProperties().entrySet() ) {
			final Object existing = augmented.put( entry.getKey(), entry.getValue() );
			changed = changed || existing != entry.getValue();
		}

		if ( changed ) {
			writeProperties(
					augmented,
					hibernatePropertiesFile,
					"Augmented for database profile `" + selectedProfile.getName() +
							"` - " + new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() )
			);
		}
	}

	public static Properties loadProperties(File propFile) {
		final Properties props = new Properties();
		try (FileInputStream stream = new FileInputStream( propFile )) {
			props.load( stream );
			return props;
		}
		catch (IOException e) {
			throw new ProfileCreationException(
					"Could not load Hibernate properties : " + propFile.getAbsolutePath(),
					e
			);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void writeProperties(
			Properties properties,
			File file,
			String comment) {
		if ( ! file.exists() ) {
			if ( ! file.getParentFile().exists() ) {
				file.getParentFile().mkdirs();
			}

			try {
				file.createNewFile();
			}
			catch (IOException e) {
				throw new RuntimeException( "Unable to create file to store Properties : " + file.getAbsolutePath(), e );
			}
		}

		try ( FileOutputStream stream = new FileOutputStream( file ) ) {
			properties.store( stream, comment );
			stream.flush();
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to store Properties to file : " + file.getAbsolutePath(), e );
		}
	}

	public static void applyProfile(
			Profile profile,
			Test testTask,
			DslExtension dslExtension,
			Project project) {
		// add an input to the test task for the selected profile for up-to-date checking
		testTask.getInputs().property( TEST_TASK_PROFILE_KEY, profile.getName() );
		testTask.getExtensions().getExtraProperties().set( TEST_TASK_PROFILE_KEY, profile.getName() );

		// add the properties
		testTask.getSystemProperties().putAll( profile.getHibernateProperties() );

		// add the extra dependencies to the test task
		testTask.getClasspath().plus( profile.getDependencies() );

		// before Test task
		profile.visitBeforeTestTaskActions(
				taskAction -> testTask.doFirst(
						task -> taskAction.accept( (Test) task )
				)
		);
		dslExtension.visitBeforeTestTaskActions(
				taskAction -> testTask.doFirst(
						task -> taskAction.accept( (Test) task )
				)
		);

		// after Test task
		dslExtension.visitAfterTestTaskActions(
				taskAction -> testTask.doLast(
						task -> taskAction.accept( (Test) task )
				)
		);
		profile.visitAfterTestTaskActions(
				taskAction -> testTask.doLast(
						task -> taskAction.accept( (Test) task )
				)
		);

		if ( dslExtension.getBeforeEachTestActions() != null || profile.getBeforeEachTestActions() != null
				|| dslExtension.getAfterEachTestActions() != null || profile.getAfterEachTestActions() != null ) {
			testTask.addTestListener(
					new TestListener() {
						@Override
						public void beforeSuite(TestDescriptor testDescriptor) {
						}

						@Override
						public void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
						}

						@Override
						public void beforeTest(TestDescriptor testDescriptor) {
							try {
								profile.visitBeforeEachTestActions(
										testDescriptorConsumer -> testDescriptorConsumer.accept( testDescriptor )
								);
								dslExtension.visitBeforeEachTestActions(
										testDescriptorConsumer -> testDescriptorConsumer.accept( testDescriptor )
								);
							}
							catch (Exception e) {
								throw new BuildExecutionException(
										"Unable to perform before-each test actions [profile = " + profile.getName() + "]",
										e
								);
							}
						}

						@Override
						public void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
							try {
								profile.visitAfterEachTestActions(
										testDescriptorConsumer -> testDescriptorConsumer.accept(
												testDescriptor,
												testResult
										)
								);
								dslExtension.visitAfterEachTestActions(
										testDescriptorConsumer -> testDescriptorConsumer.accept(
												testDescriptor,
												testResult
										)
								);
							}
							catch (Exception e) {
								throw new BuildExecutionException(
										"Unable to perform after-each test actions [profile = " + profile.getName() + "]",
										e
								);
							}
						}
					}
			);
		}
	}

	private Helper() {
		// disallow direct instantiation
	}

	public static Map<String, ?> asMap(Object... values) {
		if ( values.length %2 != 0 ) {
			throw new BuildExecutionException( "Expecting even number of values to create Map" );
		}

		final HashMap<String, Object> map = new HashMap<>();

		for ( int i = 0; i < values.length; i += 2 ) {
			if ( values[i] instanceof String ) {
				map.put( (String) values[ i ], values[ i + 1 ] );
			}
			else {
				throw new BuildExecutionException( "Map key is expected to be String : " + values[ i ] );
			}
		}

		return map;
	}
}
