/*
 * License: Apache License, Version 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.testing.db;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractActionContainer {
	private List<Consumer<Test>> beforeTestTaskActions;
	private List<Consumer<Test>> afterTestTaskActions;

	private List<Consumer<TestDescriptor>> beforeEachTestActions;
	private List<BiConsumer<TestDescriptor, TestResult>> afterEachTestActions;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Before Test task actions

	public List<Consumer<Test>> getBeforeTestTaskActions() {
		return beforeTestTaskActions;
	}

	public void setBeforeTestTaskActions(List<Consumer<Test>> beforeTestTaskActions) {
		this.beforeTestTaskActions = beforeTestTaskActions;
	}

	public void beforeTestTask(Consumer<Test> action) {
		if ( beforeTestTaskActions == null ) {
			beforeTestTaskActions = new ArrayList<>();
		}
		beforeTestTaskActions.add( action );
	}

	public void visitBeforeTestTaskActions(Consumer<Consumer<Test>> consumer) {
		if ( beforeTestTaskActions == null ) {
			return;
		}
		beforeTestTaskActions.forEach( consumer );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// After Test task actions

	public List<Consumer<Test>> getAfterTestTaskActions() {
		return afterTestTaskActions;
	}

	public void setAfterTestTaskActions(List<Consumer<Test>> afterTestTaskActions) {
		this.afterTestTaskActions = afterTestTaskActions;
	}

	public void afterTestTask(Consumer<Test> action) {
		if ( afterTestTaskActions == null ) {
			afterTestTaskActions = new ArrayList<>();
		}
		afterTestTaskActions.add( action );
	}

	public void visitAfterTestTaskActions(Consumer<Consumer<Test>> consumer) {
		if ( afterTestTaskActions == null ) {
			return;
		}
		afterTestTaskActions.forEach( consumer );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Before each test actions

	public List<Consumer<TestDescriptor>> getBeforeEachTestActions() {
		return beforeEachTestActions;
	}

	public void setBeforeEachTestActions(List<Consumer<TestDescriptor>> beforeEachTestActions) {
		this.beforeEachTestActions = beforeEachTestActions;
	}

	public void beforeEachTest(Consumer<TestDescriptor> action) {
		if ( beforeEachTestActions == null ) {
			beforeEachTestActions = new ArrayList<>();
		}
		beforeEachTestActions.add( action );
	}

	public void visitBeforeEachTestActions(Consumer<Consumer<TestDescriptor>> consumer) {
		if ( beforeEachTestActions == null ) {
			return;
		}
		beforeEachTestActions.forEach( consumer );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// After each test actions

	public List<BiConsumer<TestDescriptor, TestResult>> getAfterEachTestActions() {
		return afterEachTestActions;
	}

	public void setAfterEachTestActions(List<BiConsumer<TestDescriptor, TestResult>> afterEachTestActions) {
		this.afterEachTestActions = afterEachTestActions;
	}

	public void afterEachTest(BiConsumer<TestDescriptor,TestResult> action) {
		if ( afterEachTestActions == null ) {
			afterEachTestActions = new ArrayList<>();
		}
		afterEachTestActions.add( action );
	}

	public void visitAfterEachTestActions(Consumer<BiConsumer<TestDescriptor,TestResult>> consumer) {
		if ( afterEachTestActions == null ) {
			return;
		}
		afterEachTestActions.forEach( consumer );
	}
}
