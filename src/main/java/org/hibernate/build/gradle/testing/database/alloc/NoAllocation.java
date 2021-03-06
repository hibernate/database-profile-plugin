/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database.alloc;

import org.gradle.api.tasks.testing.Test;

/**
 * Implementation of DatabaseAllocation when no allocation should be performed
 *
 * @author Steve Ebersole
 */
class NoAllocation implements DatabaseAllocation {
    /**
     * Singleton access
     */
    public static final NoAllocation INSTANCE = new NoAllocation();

    @Override
    public void prepareForExecution(Test testTask) {
        // nothing to do
    }

    @Override
    public void beforeTestClass() {
        // nothing to do
    }

    @Override
    public void release() {
        // nothing to do
    }
}
