/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db.alloc;

import org.gradle.api.Project;

import org.hibernate.testing.db.Profile;

/**
 * @author Steve Ebersole
 */
public class StandardAllocationProvider implements AllocationProvider {
	/**
	 * Singleton access
	 */
	public static final StandardAllocationProvider INSTANCE = new StandardAllocationProvider();

	@Override
	public DatabaseAllocation createAllocation(Profile profile, Project project) {
		// todo : determine how we know which profiles to allocate...

		return NoAllocation.INSTANCE;
	}
}
