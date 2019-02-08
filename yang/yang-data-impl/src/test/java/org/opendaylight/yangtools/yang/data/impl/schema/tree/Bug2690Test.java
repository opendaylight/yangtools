/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class Bug2690Test extends AbstractTestModelTest {
    private DataTree inMemoryDataTree;

    @Before
    public void prepare() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            SCHEMA_CONTEXT);
    }

    @Test
    public void testWriteMerge1() throws DataValidationFailedException {
        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2);
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(fooEntryNode).build();
        final MapNode mapNode2 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(barEntryNode).build();

        final ContainerNode cont1 = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode1).build();

        final ContainerNode cont2 = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode2).build();

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, cont1);
        modificationTree.merge(TestModel.TEST_PATH, cont2);
        commit(modificationTree);

        final DataTreeSnapshot snapshotAfterTx = inMemoryDataTree.takeSnapshot();
        final DataTreeModification modificationAfterTx = snapshotAfterTx.newModification();
        final Optional<NormalizedNode<?, ?>> readNode = modificationAfterTx.readNode(TestModel.OUTER_LIST_PATH);
        assertTrue(readNode.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?,?,?>)readNode.get()).getValue().size());
    }

    @Test
    public void testDeleteStructuralAndWriteChild() throws DataValidationFailedException {
        final DataTreeModification modificationTree = setupTestDeleteStructuralAndWriteChild();
        verifyTestDeleteStructuralAndWriteChild(modificationTree);
    }

    @Test
    public void testDeleteStructuralAndWriteChildWithCommit() throws DataValidationFailedException {
        final DataTreeModification modificationTree = setupTestDeleteStructuralAndWriteChild();
        commit(modificationTree);
        verifyTestDeleteStructuralAndWriteChild(inMemoryDataTree.takeSnapshot());
    }

    private DataTreeModification setupTestDeleteStructuralAndWriteChild() {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.delete(TestModel.NON_PRESENCE_PATH);
        modificationTree.write(TestModel.NAME_PATH, ImmutableNodes.leafNode(TestModel.NAME_QNAME, "abc"));
        return modificationTree;
    }

    private static void verifyTestDeleteStructuralAndWriteChild(final DataTreeSnapshot snapshot) {
        final Optional<NormalizedNode<?, ?>> readNode = snapshot.readNode(TestModel.NAME_PATH);
        assertTrue(readNode.isPresent());
    }

    private void commit(final DataTreeModification modificationTree) throws DataValidationFailedException {
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
