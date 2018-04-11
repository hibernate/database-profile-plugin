/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static Properties loadProperties(File propFile) {
		try (FileInputStream stream = new FileInputStream( propFile )) {
			final Properties props = new Properties();
			props.load( stream );
			return props;
		}
		catch (IOException e) {
			throw new BuildException( "Unable to load Properties from file : " + propFile.getAbsolutePath(), e );
		}
	}

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
				throw new BuildException( "Unable to create file to store Properties : " + file.getAbsolutePath(), e );
			}
		}

		try ( FileOutputStream stream = new FileOutputStream( file ) ) {
			properties.store( stream, comment );
			stream.flush();
		}
		catch (IOException e) {
			throw new BuildException( "Unable to store Properties to file : " + file.getAbsolutePath(), e );
		}
	}
}
