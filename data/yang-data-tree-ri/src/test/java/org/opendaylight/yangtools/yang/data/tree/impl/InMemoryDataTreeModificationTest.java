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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.util.Optional;
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
import org.opendaylight.yangtools.yang.data.tree.api.VersionInfo;
import org.opendaylight.yangtools.yang.data.tree.impl.InMemoryDataTreeModification.State;

/**
 * {@link InMemoryDataTreeModification} state transitions. Relies on Mockito's mocking of final classes to control code
 * flow and inject unexpected errors;
 */
@ExtendWith(MockitoExtension.class)
class InMemoryDataTreeModificationTest extends AbstractTestModelTest {
    private static final DataTreeConfiguration TREE_CONFIG = DataTreeConfiguration.DEFAULT_CONFIGURATION.copyBuilder()
        .setTrackVersionInfo(true)
        .build();

    @Mock
    private DataTreeModificationCursor cursor;
    @Mock
    private VersionInfo versionInfo;

    private DataTree tree;
    private InMemoryDataTreeModification mod;

    @BeforeEach
    void beforeEach() {
        tree = new ReferenceDataTreeFactory().create(TREE_CONFIG, MODEL_CONTEXT);
        final var real = assertInstanceOf(InMemoryDataTreeModification.class, tree.takeSnapshot().newModification());
        mod = spy(real);
    }

    @Test
    void testToString() {
        assertEquals("InMemoryDataTreeModification{state=Open}", mod.toString());
        mod.ready();
        assertEquals("InMemoryDataTreeModification{state=Noop}", mod.toString());
    }

    @Test
    void testReadyNewModicationValidatePrepare() throws Exception {
        mod.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        mod.ready();
        assertState("Ready");

        // transitions state
        final var firstMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        final var applied = assertState("AppliedToSnapshot");
        assertNotSame(mod, firstMod);
        assertNotSame(mod.snapshotRoot(), firstMod.snapshotRoot());

        // does not transition state, but reuses underlying snapshot root node
        final var secondMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertState(applied);
        assertNotSame(mod, secondMod);
        assertNotSame(mod.snapshotRoot(), secondMod.snapshotRoot());
        assertNotSame(firstMod, secondMod);
        assertSame(firstMod.snapshotRoot(), secondMod.snapshotRoot());

        // validate is a no-op as we have the DataTree
        tree.validate(mod);
        assertState(applied);

        // transitions state
        final var candidate = assertInstanceOf(InMemoryDataTreeCandidate.class, tree.prepare(mod));
        final var prepared = assertState("Prepared");

        tree.commit(candidate, versionInfo);
        // TODO: we really would like to have a dedicated state which routes to the data tree for 'newModification'
        //       et al.
        assertState(prepared);

        assertEquals(Optional.of(versionInfo), tree.takeSnapshot().readVersionInfo(YangInstanceIdentifier.of()));

        // does not transition state and reports same root as candidate's dataAfter
        final var thirdMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertState(prepared);
        assertNotSame(mod, thirdMod);
        assertNotSame(firstMod, thirdMod);
        assertNotSame(secondMod, thirdMod);

        assertNotSame(mod.snapshotRoot(), thirdMod.snapshotRoot());
        assertNotSame(firstMod.snapshotRoot(), thirdMod.snapshotRoot());
        assertSame(candidate.getTipRoot(), thirdMod.snapshotRoot());

        // a further validate() is a no-op
        tree.validate(mod);
        assertState(prepared);
    }

    @Test
    void testOpenEmptyApplyToCursor() {
        mod.applyToCursor(cursor);
        assertState("Open");
    }

    @Test
    void testNoopApplyToCursor() {
        mod.ready();
        final var state = assertState("Noop");
        mod.applyToCursor(cursor);
        assertState(state);
    }

    @Test
    void testReadyApplyCursor() {
        final var data = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
        mod.write(TestModel.TEST_PATH, data);
        mod.ready();
        final var ready = assertState("Ready");

        doNothing().when(cursor).write(eq(data.name()), same(data));
        mod.applyToCursor(cursor);
        assertState(ready);
    }

    @Test
    void testNewModificationApplyCursor() {
        final var data = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
        mod.write(TestModel.TEST_PATH, data);
        mod.ready();
        assertState("Ready");

        assertNotNull(mod.newModification());
        final var applied = assertState("AppliedToSnapshot");

        doNothing().when(cursor).write(eq(data.name()), same(data));
        mod.applyToCursor(cursor);
        assertState(applied);
    }

    @Test
    void testNoopNewModification() {
        mod.ready();
        final var state = assertState("Noop");

        final var next = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertState(state);
        assertSame(mod.snapshotRoot(), next.snapshotRoot());
    }

    @Test
    void testReadyDefunct() {
        // fixed thread name for assertions
        final var threadName = UUID.randomUUID().toString();
        final var cause = new Throwable("some text");
        // sneaky throw
        doAnswer(inv -> {
            Thread.currentThread().setName(threadName);
            throw cause;
        }).when(mod).runReady(any());

        mod.delete(TestModel.TEST_PATH);
        assertSame(cause, assertThrowsExactly(Throwable.class, mod::ready));

        final var defunct = assertState("Defunct{threadName=" + threadName + ", cause=" + cause + "}");
        assertISE(defunct, cause, "ready", mod::ready);
        assertISE(defunct, cause, "chain on", mod::newModification);
        assertISE(defunct, cause, "access data of", () -> mod.readNode(YangInstanceIdentifier.of()));

        assertIAE(defunct, cause, "validate", () -> tree.validate(mod));
        assertIAE(defunct, cause, "prepare", () -> tree.prepare(mod));

        doNothing().when(cursor).delete(new NodeIdentifier(TestModel.TEST_QNAME));
        mod.applyToCursor(cursor);
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
        assertState(defunct);
    }

    private State assertState(final String expected) {
        final var state = mod.acquireState();
        assertEquals(expected, state.toString());
        return state;
    }

    private void assertState(final State expected) {
        assertSame(expected, mod.acquireState());
    }
}
