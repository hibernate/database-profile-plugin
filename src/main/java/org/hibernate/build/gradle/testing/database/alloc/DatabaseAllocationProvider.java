/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.testing.database.alloc;

import org.gradle.api.Project;

import org.hibernate.testing.db.Profile;

/**
 * @author Steve Ebersole
 */
public interface DatabaseAllocationProvider {
	DatabaseAllocation buildAllocation(Project rootProject, Profile profile, DatabaseAllocationCleanUp cleanUp);
}
