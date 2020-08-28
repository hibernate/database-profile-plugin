/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db.alloc;

import org.gradle.api.Project;

import org.hibernate.testing.db.DslExtension;

/**
 * Delegate for managing dynamic database instance allocation as part of the testing lifecycle.
 *
 *
 *
 * Helper for dealing with the "DB Allocator" service set up in the JBoss/Red Hat QE lab.
 *
 * Use the <code>hibernate-matrix-dballocation</code> setting to control db allocation.  By default,
 * no allocations are performed.  <code>hibernate-matrix-dballocation</code> could be either:<ul>
 *     <li><b>all</b> - allocate all non-ignored databases</li>
 *     <li><b>profile1{,profile2,...}</b> - allocate only the named profiles, provided the name is also one of the supported names</li>
 * </ul>
 *
 * @author mvecera
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class DatabaseAllocator {
    public static void apply(DslExtension dslExtension, Project project) {
        final AllocationRegistry allocationRegistry = new AllocationRegistry();
        project.getGradle().addBuildListener( allocationRegistry );

        final AllocationProvider allocationProvider = StandardAllocationProvider.INSTANCE;

        dslExtension.getProfiles().forEach(
                profile -> {
                    final DatabaseAllocation allocation = allocationProvider.createAllocation( profile, project );
                    allocationRegistry.registerAllocation( profile, allocation );
                }
        );
    }
}
