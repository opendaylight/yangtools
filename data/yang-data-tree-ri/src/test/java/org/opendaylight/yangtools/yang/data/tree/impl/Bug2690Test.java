/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

class Bug2690Test extends AbstractTestModelTest {
    private DataTree inMemoryDataTree;

    @BeforeEach
    void prepare() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            SCHEMA_CONTEXT);
    }

    @Test
    void testWriteMerge1() throws DataValidationFailedException {
        final var fooEntryNode = ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 1))
            .build();
        final var barEntryNode = ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2))
            .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 2))
            .build();
        final var mapNode1 = ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(fooEntryNode).build();
        final var mapNode2 = ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(barEntryNode).build();

        final var cont1 = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode1).build();

        final var cont2 = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode2).build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, cont1);
        modificationTree.merge(TestModel.TEST_PATH, cont2);
        commit(modificationTree);

        final var snapshotAfterTx = inMemoryDataTree.takeSnapshot();
        final var modificationAfterTx = snapshotAfterTx.newModification();
        final var readNode = modificationAfterTx.readNode(TestModel.OUTER_LIST_PATH);
        assertTrue(readNode.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?>)readNode.orElseThrow()).size());
    }

    @Test
    void testDeleteStructuralAndWriteChild() throws DataValidationFailedException {
        final var modificationTree = setupTestDeleteStructuralAndWriteChild();
        verifyTestDeleteStructuralAndWriteChild(modificationTree);
    }

    @Test
    void testDeleteStructuralAndWriteChildWithCommit() throws DataValidationFailedException {
        final var modificationTree = setupTestDeleteStructuralAndWriteChild();
        commit(modificationTree);
        verifyTestDeleteStructuralAndWriteChild(inMemoryDataTree.takeSnapshot());
    }

    private DataTreeModification setupTestDeleteStructuralAndWriteChild() {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.delete(TestModel.NON_PRESENCE_PATH);
        modificationTree.write(TestModel.NAME_PATH, ImmutableNodes.leafNode(TestModel.NAME_QNAME, "abc"));
        return modificationTree;
    }

    private static void verifyTestDeleteStructuralAndWriteChild(final DataTreeSnapshot snapshot) {
        final var readNode = snapshot.readNode(TestModel.NAME_PATH);
        assertTrue(readNode.isPresent());
    }

    private void commit(final DataTreeModification modificationTree) throws DataValidationFailedException {
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
