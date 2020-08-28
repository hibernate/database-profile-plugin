/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db;

/**
 * Indicates a request to create a profile using a name that already exists
 *
 * @author Steve Ebersole
 */
public class DuplicateProfileException extends RuntimeException {
	private final String profileName;

	public DuplicateProfileException(String profileName) {
		super( "A profile with the name `" + profileName + "` already existed" );

		this.profileName = profileName;
	}

	public String getProfileName() {
		return profileName;
	}
}
