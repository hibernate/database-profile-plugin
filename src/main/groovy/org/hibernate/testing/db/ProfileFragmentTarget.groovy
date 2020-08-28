/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db

/**
 * Used as the target for applying a profile fragment.  Handles creating the
 * Profile instance and adding it to the profiles container.
 *
 * @see ProfileLoader
 *
 * @author Steve Ebersole
 */
class ProfileFragmentTarget {
	private final DslExtension dslExtension
	private final ProfileCreator profileCreator;

	ProfileFragmentTarget(DslExtension dslExtension, ProfileCreator profileCreator) {
		this.dslExtension = dslExtension
		this.profileCreator = profileCreator;
	}

	void methodMissing(String name, args) {
		final Closure<?> closure = (Closure<?>) args[0];

		if ( name == Helper.DSL_NAME ) {
			closure.setDelegate( dslExtension );
			closure.call();
		}
		else {
			final Profile profile = profileCreator.create( name );

			closure.setDelegate( profile );
			closure.call();

			dslExtension.profiles.add( profile );
		}
	}
}
