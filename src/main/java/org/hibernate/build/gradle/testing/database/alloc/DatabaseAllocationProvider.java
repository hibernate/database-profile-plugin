package org.hibernate.build.gradle.testing.database.alloc;

import org.gradle.api.Project;

import org.hibernate.build.gradle.testing.database.Profile;

/**
 * @author Steve Ebersole
 */
public interface DatabaseAllocationProvider {
	DatabaseAllocation buildAllocation(Project rootProject, Profile profile, DatabaseAllocationCleanUp cleanUp);
}
