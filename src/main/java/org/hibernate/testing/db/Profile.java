/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import groovy.lang.Closure;

/**
 * General contract for a database profile.
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class Profile extends AbstractActionContainer {
	public static final String DIALECT = "hibernate.dialect";
	public static final String DRIVER = "hibernate.connection.driver_class";
	public static final String URL = "hibernate.connection.url";
	public static final String USERNAME = "hibernate.connection.username";
	public static final String PASSWORD = "hibernate.connection.password";

	private final String name;
	private final Configuration dependencies;

	private final Project project;

	private Map<String, Object> hibernateProperties = new HashMap<>();

	public Profile(String name, Configuration dependencies, Project project) {
		this.name = name;
		this.dependencies = dependencies;
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public Project getDefiningProject() {
		return project;
	}

	public Configuration getDependencies() {
		return dependencies;
	}

	public void dependency(Object notation) {
		project.getDependencies().add( dependencies.getName(), notation );
	}

	public void dependency(Object notation, Closure<?> config) {
		project.getDependencies().add( dependencies.getName(), notation, config );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Hibernate properties

	public Map<String, Object> getHibernateProperties() {
		return hibernateProperties;
	}

	public void setHibernateProperties(Map<String, Object> hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	void applyHibernateProperties(Map properties) {
		if ( this.hibernateProperties == null ) {
			this.hibernateProperties = new HashMap<>();
		}
		else {
			this.hibernateProperties.clear();
		}
		this.hibernateProperties.putAll( properties );
	}

	public void hibernateProperty(String name, Object value) {
		property( name, value );
	}

	public void property(String name, Object value) {
		if ( hibernateProperties == null ) {
			hibernateProperties = new HashMap<>();
		}
		hibernateProperties.put( name, value );
	}

	public void dialect(String dialect) {
		property( DIALECT, dialect );
	}

	public void driver(String driverClassName) {
		property( DRIVER, driverClassName );
	}

	public void url(String url) {
		property( URL, url );
	}

	public void username(String username) {
		property( USERNAME, username );
	}

	public void password(String password) {
		property( PASSWORD, password );
	}

}
