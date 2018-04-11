/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
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
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import org.hibernate.build.gradle.testing.Helper;

/**
 * Basic support for {@link Profile} implementations
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public abstract class AbstractProfileImpl implements Profile {
    private final String name;
	private final File profileDirectory;
	private final Project project;
	private final Map<String,Object> hibernateProperties;

	@SuppressWarnings({"unchecked", "WeakerAccess"})
	protected AbstractProfileImpl(File profileDirectory, Project project) {
		this.profileDirectory = profileDirectory;
		this.name = profileDirectory.getName();
		this.project = project;

		final File hibernatePropertiesFile = new File(
				new File( profileDirectory, "resources" ),
				"hibernate.properties"
		);

		final Map<String,Object> inflight = new HashMap();
		Helper.loadPropertiesIfFileExists( hibernatePropertiesFile ).entrySet().forEach(
				entry -> {
					if ( entry.getKey() instanceof String ) {
						inflight.put( (String) entry.getKey(), entry.getValue() );
					}
				}
		);
		this.hibernateProperties = Collections.unmodifiableMap( inflight );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public File getDirectory() {
		return profileDirectory;
	}

	@Override
	public Map<String, Object> getHibernateProperties() {
		return hibernateProperties;
	}

	protected Configuration prepareConfiguration(String name) {
        Configuration configuration = getOrCreateConfiguration( name );
        configuration.setDescription( "The JDBC dependency configuration for the [" + name + "] profile" );
        return configuration;
    }

    protected Configuration getOrCreateConfiguration(String name) {
        Configuration configuration = project.getConfigurations().findByName( name );
        if ( configuration == null ) {
            configuration = project.getConfigurations().create( name );
        }
        return configuration;
    }
}
