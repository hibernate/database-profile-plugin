package org.hibernate.build.gradle.testing.database.alloc;

import org.gradle.api.tasks.testing.Test;

/**
 * Implementation of DatabaseAllocation when no allocation was found.
 *
 * @author Steve Ebersole
 */
class NoAllocation implements DatabaseAllocation {
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
