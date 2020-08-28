/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db.alloc;

import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;

/**
 * Represents a database instances allocated in the JBoss/Red Hat Qe Lab via {@link DatabaseAllocator}
 *
 * @see DatabaseAllocator
 *
 * @author mvecera
 * @author Strong Liu
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public interface DatabaseAllocation {
	void beforeAllTests(Test task);

	void beforeEachTest(TestDescriptor testDescriptor);

	void release();
}
