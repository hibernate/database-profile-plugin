/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

/**
 * Indicates a problem creation a Profile
 *
 * @author Steve Ebersole
 */
public class ProfileCreationException extends RuntimeException {
	public ProfileCreationException(String message) {
		super( message );
	}

	public ProfileCreationException(String message, Throwable cause) {
		super( message, cause );
	}
}
