/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db.alloc;

import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;


/**
 * Implementation of DatabaseAllocation when no allocation was found.
 *
 * @author Steve Ebersole
 */
class NoAllocation implements DatabaseAllocation {
    /**
     * Singleton access
     */
    public static final NoAllocation INSTANCE = new NoAllocation();

    @Override
    public void beforeAllTests(Test task) {
    }

    @Override
    public void beforeEachTest(TestDescriptor testDescriptor) {
    }

    @Override
    public void release() {
    }
}
