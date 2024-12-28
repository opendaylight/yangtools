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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class InMemoryDataTreeModificationTest extends AbstractTestModelTest {
    private static final DataTreeConfiguration TREE_CONFIG = DataTreeConfiguration.DEFAULT_CONFIGURATION;

    @Mock
    private DataTreeModificationCursor cursor;

    private DataTree tree;
    private InMemoryDataTreeModification mod;

    @BeforeEach
    void beforeEach() {
        tree = new InMemoryDataTreeFactory().create(TREE_CONFIG, SCHEMA_CONTEXT);
        final var real = assertInstanceOf(InMemoryDataTreeModification.class, tree.takeSnapshot().newModification());
        mod = spy(real);
    }

    @Test
    void testReadyNewModicationValidatePrepare() throws Exception {
        mod.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        mod.ready();
        final var ready = assertState("Ready");

        // transitions state
        final var firstMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertNotSame(mod, firstMod);
        assertNotSame(mod.snapshotRoot(), firstMod.snapshotRoot());
        assertState(ready);

        // does not transition state, but reuses underlying snapshot root node
        final var secondMod = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertState(ready);
        assertNotSame(mod, secondMod);
        assertNotSame(mod.snapshotRoot(), secondMod.snapshotRoot());
        assertNotSame(firstMod, secondMod);
        // FIXME: we should return the same snapshot
        assertNotSame(firstMod.snapshotRoot(), secondMod.snapshotRoot());

        // validate is a no-op as we have already
        tree.validate(mod);
        assertState(ready);

        final var candidate = tree.prepare(mod);
        assertNotNull(candidate);
        // FIXME: candidate does not transition for now: extend checks once it does, including a subsequent
        //        newModification()
        assertState(ready);

        tree.commit(candidate);
        // TODO: we really would like to have a dedicated state which routes to the data tree for 'newModification'
        //       et al.
        assertState(ready);
    }

    @Test
    void testOpenEmptyApplyToCursor() {
        mod.applyToCursor(cursor);
        assertState("Open");
    }

    @Test
    void testNoopApplyToCursor() {
        mod.ready();
        final var state = assertState("Ready");
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
        final var applied = assertState("Ready");

        doNothing().when(cursor).write(eq(data.name()), same(data));
        mod.applyToCursor(cursor);
        assertState(applied);
    }

    @Test
    void testNoopNewModification() {
        mod.ready();
        final var state = assertState("Ready");

        final var next = assertInstanceOf(InMemoryDataTreeModification.class, mod.newModification());
        assertState(state);
        assertSame(mod.snapshotRoot(), next.snapshotRoot());
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
