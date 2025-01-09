/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.impl.InMemoryDataTreeModification.State;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

/**
 * {@link InMemoryDataTreeModification} state transitions. Relies on Mockito's mocking of final classes to control code
 * flow and inject unexpected errors;
 */
@ExtendWith(MockitoExtension.class)
class InMemoryDataTreeModificationTransitionsTest extends AbstractTestModelTest {
    @Mock
    private DataTreeModificationCursor cursor;
    private DataTree tree;
    private InMemoryDataTreeModification mod;

    @BeforeEach
    void beforeEach() {
        tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);
        final var real = assertInstanceOf(InMemoryDataTreeModification.class, tree.takeSnapshot().newModification());
        mod = spy(real);
    }

    @Test
    void testReadyDefunct() {
        final var threadName = UUID.randomUUID().toString();
        final var cause = new Throwable("some text");
        doAnswer(inv -> {
            // propagate thread name and perform the equivalent of a sneaky throws
            Thread.currentThread().setName(threadName);
            throw cause;
        }).when(mod).runReady(any());

        mod.delete(TestModel.TEST_PATH);
        assertSame(cause, assertThrowsExactly(Throwable.class, mod::ready));

        final var defunct = mod.acquireState();
        assertEquals("Defunct{threadName=" + threadName + ", cause=" + cause + "}", defunct.toString());

        assertISE(defunct, cause, "ready", mod::ready);
        assertISE(defunct, cause, "chain on", mod::newModification);
        assertISE(defunct, cause, "access data of", () -> mod.readNode(YangInstanceIdentifier.of()));
        assertISE(defunct, cause, "access contents of", () -> mod.applyToCursor(cursor));

        assertIAE(defunct, cause, "validate", () -> tree.validate(mod));
        assertIAE(defunct, cause, "prepare", () -> tree.prepare(mod));
    }

    private void assertIAE(final State defunct, final Throwable cause, final String op, final Executable executable) {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, executable);
        final var ise = assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertSame(cause, ise.getCause());
        assertDefunct(defunct, op, ise);
    }

    private void assertISE(final State defunct, final Throwable cause, final String op, final Executable executable) {
        final var ex = assertThrowsExactly(IllegalStateException.class, executable);
        assertSame(cause, ex.getCause());
        assertDefunct(defunct, op, ex);
    }

    private void assertDefunct(final State defunct, final String op, final IllegalStateException ex) {
        assertEquals("Attempted to " + op + " modification in state " + defunct, ex.getMessage());
        assertSame(defunct, mod.acquireState());
    }

    @Test
    void testReadyNewModicationValidatePrepare() throws Exception {
        mod.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        mod.ready();
        assertEquals("Ready", mod.acquireState().toString());

        // transitions state
        final var firstMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertNotSame(mod, firstMod);
        assertNotSame(mod.snapshotRoot(), firstMod.snapshotRoot());
        final var applied = mod.acquireState();
        assertEquals("AppliedToSnapshot", applied.toString());

        // does not transition state, but reuses underlying snapshot root node
        final var secondMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertSame(applied, mod.acquireState());
        assertNotSame(mod, secondMod);
        assertNotSame(mod.snapshotRoot(), secondMod.snapshotRoot());
        assertNotSame(firstMod, secondMod);
        assertSame(firstMod.snapshotRoot(), secondMod.snapshotRoot());

        // validate is a no-op as we have already
        tree.validate(mod);
        assertSame(applied, mod.acquireState());

        // transitions state
        final var candidate = assertInstanceOf(InMemoryDataTreeCandidate.class, tree.prepare(mod));
        final var prepared = mod.acquireState();
        assertEquals("Prepared", prepared.toString());

        // does not transition state and reports same root as candidate's dataAfter
        final var thirdMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertSame(prepared, mod.acquireState());
        assertNotSame(mod, thirdMod);
        assertNotSame(firstMod, thirdMod);
        assertNotSame(secondMod, thirdMod);

        assertNotSame(mod.snapshotRoot(), thirdMod.snapshotRoot());
        assertNotSame(firstMod.snapshotRoot(), thirdMod.snapshotRoot());
        assertSame(candidate.getTipRoot(), thirdMod.snapshotRoot());
    }

    @Test
    void testOpenEmptyApplyToCursor() {
        mod.applyToCursor(cursor);
        assertEquals("Open", mod.acquireState().toString());
    }

    @Test
    void testNoopApplyToCursor() {
        mod.ready();
        final var state = mod.acquireState();
        assertEquals("Noop", state.toString());
        mod.applyToCursor(cursor);
        assertSame(state, mod.acquireState());
    }

    @Test
    void testReadyApplyCursor() {
        final var data = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
        mod.write(TestModel.TEST_PATH, data);
        mod.ready();
        final var ready = mod.acquireState();
        assertEquals("Ready", ready.toString());

        doNothing().when(cursor).write(eq(data.name()), same(data));
        mod.applyToCursor(cursor);
        assertSame(ready, mod.acquireState());
    }

    @Test
    void testNewModificationApplyCursor() {
        final var data = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
        mod.write(TestModel.TEST_PATH, data);
        mod.ready();
        assertEquals("Ready", mod.acquireState().toString());

        assertNotNull(mod.newModification());
        final var applied = mod.acquireState();
        assertEquals("AppliedToSnapshot", applied.toString());

        doNothing().when(cursor).write(eq(data.name()), same(data));
        mod.applyToCursor(cursor);
        assertSame(applied, mod.acquireState());
    }

    @Test
    void testNoopNewModification() {
        mod.ready();
        final var state = mod.acquireState();
        assertEquals("Noop", state.toString());

        final var next = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertSame(state, mod.acquireState());
        assertSame(mod.snapshotRoot(), next.snapshotRoot());
    }
}
