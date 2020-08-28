/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

import java.util.function.Consumer;

/**
 * Access to all known profiles.
 *
 * @author Steve Ebersole
 */
public interface ProfileAccess {
	/**
	 * Find a profile by name.
	 */
	Profile findProfile(String name);

	/**
	 * Get a profile by name.  Delegates to {@link #findProfile} throwing an exception
	 * if a profile by that name does not exist
	 */
	Profile getProfile(String name) throws UnknownProfileException;

	/**
	 * Visit all profiles
	 */
	void visitProfiles(Consumer<Profile> profileConsumer);

	/**
	 * Visit the names of all profiles
	 */
	void visitProfileNames(Consumer<String> profileNameConsumer);
}
