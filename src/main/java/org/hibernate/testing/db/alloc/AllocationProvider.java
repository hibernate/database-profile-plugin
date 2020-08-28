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
public interface AllocationProvider {
	DatabaseAllocation createAllocation(Profile profile, Project project);
}
