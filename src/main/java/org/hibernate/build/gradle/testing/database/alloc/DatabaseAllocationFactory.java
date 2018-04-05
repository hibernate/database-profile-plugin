package org.hibernate.build.gradle.testing.database.alloc;

import org.hibernate.build.gradle.testing.database.Profile;

/**
 * @author Steve Ebersole
 */
public interface DatabaseAllocationFactory {
	DatabaseAllocation getDatabaseAllocation(Profile profile);
}
