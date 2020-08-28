/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.db;

/**
 * @author Steve Ebersole
 */
public class UnknownProfileException extends RuntimeException {
	static UnknownProfileException forName(String unknownName) {
		return new UnknownProfileException(
				"Unknown database-profile name : " + unknownName
		);
	}

	UnknownProfileException(String message) {
		super( message );
	}
}
