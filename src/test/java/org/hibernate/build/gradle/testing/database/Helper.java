/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static final String DIALECT_PROP_KEY = "hibernate.dialect";

	// our output is the base for all of the test projects, so we must be
	// able to locate that..  this is our `${buildDir}/resources/test` directory
	// within that directory we will have access to the `databases` dir as well
	// as the various test project root dirs
	public static File testProjectsBaseDirectory() {
		final URL baseUrl = Helper.class.getResource( "/db-profile-testResource-locator.properties" );
		return new File( baseUrl.getFile() ).getParentFile();
	}

	public static File projectDirectory(String projectName) {
		return new File( testProjectsBaseDirectory(), "testing/" + projectName );
	}

	public static Properties projectTestProperties(String projectName) {
		final File propFile = new File(
				projectDirectory( projectName ),
				"build/resources/test/hibernate.properties"
		);

		try (FileInputStream stream = new FileInputStream( propFile )) {
			final Properties props = new Properties();
			props.load( stream );
			return props;
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to load project's output `hibernate.properties` file", e );
		}
	}

	public static File findResourcesOutputDir() {
		final URL url = Helper.class.getResource( "/db-profile-testResource-locator.properties" );

		// this is the test project's
		return new File( url.getFile() ).getParentFile();
	}
}
