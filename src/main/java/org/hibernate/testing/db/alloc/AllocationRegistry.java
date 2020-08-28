/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db.alloc;

import java.util.HashMap;
import java.util.Map;

import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

import org.hibernate.testing.db.Profile;

/**
 * @author Steve Ebersole
 */
public class AllocationRegistry implements BuildListener {
	private Map<Profile, DatabaseAllocation> databaseAllocationMap;

	public void registerAllocation(Profile profile, DatabaseAllocation allocation) {
		if ( databaseAllocationMap == null ) {
			databaseAllocationMap = new HashMap<>();
		}
		databaseAllocationMap.put( profile, allocation );
	}

	public DatabaseAllocation findAllocation(Profile profile) {
		return databaseAllocationMap.get( profile );
	}

	public void release() {
		databaseAllocationMap.forEach( (profile, allocation) -> allocation.release() );
		databaseAllocationMap.clear();
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// BuildListener

	@Override
	public void buildStarted(Gradle gradle) {
		// don't care
	}

	@Override
	public void settingsEvaluated(Settings settings) {
		// don't care
	}

	@Override
	public void projectsLoaded(Gradle gradle) {
		// don't care
	}

	@Override
	public void projectsEvaluated(Gradle gradle) {
		// don't care
	}

	@Override
	public void buildFinished(BuildResult buildResult) {
		release();
	}
}
