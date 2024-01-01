/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class DataTreeCandidatesTest extends AbstractTestModelTest {
    private DataTree dataTree;

    @BeforeEach
    void setUp() throws Exception {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

        final var testContainer = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
                .build())
            .build();

        final var modification = (InMemoryDataTreeModification) dataTree.takeSnapshot()
                .newModification();
        final var cursor = modification.openCursor();
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        modification.ready();

        dataTree.validate(modification);
        final var candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);
    }

    @Test
    void testRootedCandidate() throws DataValidationFailedException {
        final var innerDataTree = new InMemoryDataTreeFactory().create(
            new DataTreeConfiguration.Builder(TreeType.OPERATIONAL)
            .setMandatoryNodesValidation(true)
            .setRootPath(TestModel.INNER_CONTAINER_PATH)
            .setUniqueIndexes(true).build(), SCHEMA_CONTEXT);

        final var leaf = ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "testing-value");

        final var modification = innerDataTree.takeSnapshot().newModification();
        modification.write(TestModel.VALUE_PATH, leaf);

        modification.ready();
        dataTree.validate(modification);
        final var candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);

        final var newModification = dataTree.takeSnapshot().newModification();
        final var newCandidate = DataTreeCandidates.newDataTreeCandidate(TestModel.INNER_CONTAINER_PATH,
            candidate.getRootNode());

        // lets see if getting the identifier of the root node throws an exception
        assertThrows(IllegalStateException.class, () ->  newCandidate.getRootNode().name());

        // lets see if we can apply this rooted candidate to a new dataTree
        DataTreeCandidates.applyToModification(newModification,
                newCandidate);

        final var readLeaf = (LeafNode<?>) newModification.readNode(TestModel.INNER_VALUE_PATH).orElseThrow();
        assertEquals(readLeaf, leaf);
    }

    @Test
    void testEmptyMergeOnContainer() throws DataValidationFailedException {
        final var modification = dataTree.takeSnapshot().newModification();
        modification.merge(TestModel.NON_PRESENCE_PATH, ImmutableNodes.containerNode(TestModel.NON_PRESENCE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        // The entire transaction needs to fizzle to a no-op
        final var candidate = dataTree.prepare(modification);
        final var node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.modificationType());

        // 'test'
        assertUnmodified(1, node.childNodes());
    }

    @Test
    void testEmptyWriteOnContainer() throws DataValidationFailedException {
        final var modification = dataTree.takeSnapshot().newModification();
        modification.write(TestModel.NON_PRESENCE_PATH, ImmutableNodes.containerNode(TestModel.NON_PRESENCE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        // The entire transaction needs to fizzle to a no-op
        final var candidate = dataTree.prepare(modification);
        final var node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.modificationType());

        // 'test'
        assertUnmodified(1, node.childNodes());
    }

    @Test
    void testEmptyMergesOnDeleted() throws DataValidationFailedException {
        final var modification = dataTree.takeSnapshot().newModification();
        modification.delete(TestModel.NON_PRESENCE_PATH);
        modification.merge(TestModel.DEEP_CHOICE_PATH, ImmutableNodes.choiceNode(TestModel.DEEP_CHOICE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        final var candidate = dataTree.prepare(modification);
        assertEquals(YangInstanceIdentifier.of(), candidate.getRootPath());
        final var node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.modificationType());

        // 'test'
        assertUnmodified(1, node.childNodes());
    }

    @Test
    void testEmptyMergesOnExisting() throws DataValidationFailedException {
        // Make sure 'non-presence' is present
        var modification = dataTree.takeSnapshot().newModification();
        modification.write(TestModel.NAME_PATH, ImmutableNodes.leafNode(TestModel.NAME_QNAME, "foo"));
        modification.ready();
        dataTree.validate(modification);
        dataTree.commit(dataTree.prepare(modification));

        // Issue an empty merge on it and a child choice
        modification = dataTree.takeSnapshot().newModification();
        modification.merge(TestModel.NON_PRESENCE_PATH, ImmutableNodes.containerNode(TestModel.NON_PRESENCE_QNAME));
        modification.merge(TestModel.DEEP_CHOICE_PATH, ImmutableNodes.choiceNode(TestModel.DEEP_CHOICE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        // The entire transaction needs to fizzle to a no-op
        final var candidate = dataTree.prepare(modification);
        assertEquals(YangInstanceIdentifier.of(), candidate.getRootPath());
        final var node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.modificationType());

        // 'non-presence' and 'test'
        assertUnmodified(2, node.childNodes());
    }

    @Test
    void testAggregateWithoutChanges() throws DataValidationFailedException {
        final var modification1 = dataTree.takeSnapshot().newModification();
        modification1.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value1"));
        modification1.ready();
        dataTree.validate(modification1);
        DataTreeCandidate candidate1 = dataTree.prepare(modification1);
        dataTree.commit(candidate1);

        final var modification2 = dataTree.takeSnapshot().newModification();
        modification2.delete(TestModel.INNER_CONTAINER_PATH);
        modification2.ready();
        dataTree.validate(modification2);
        final var candidate2 = dataTree.prepare(modification2);
        dataTree.commit(candidate2);

        final var aggregateCandidate = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregateCandidate.getRootNode().modificationType());
    }

    @Test
    void testAggregate() throws DataValidationFailedException {
        final var modification = dataTree.takeSnapshot().newModification();
        modification.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value1"));
        modification.ready();
        dataTree.validate(modification);
        final var candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);

        final var modification1 = dataTree.takeSnapshot().newModification();
        modification1.delete(TestModel.INNER_CONTAINER_PATH);
        modification1.ready();
        dataTree.validate(modification1);
        DataTreeCandidate candidate1 = dataTree.prepare(modification1);
        dataTree.commit(candidate1);

        final var modification2 = dataTree.takeSnapshot().newModification();
        modification2.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value2"));
        modification2.ready();
        dataTree.validate(modification2);
        final var candidate2 = dataTree.prepare(modification2);
        dataTree.commit(candidate2);

        final var aggregateCandidate = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED,aggregateCandidate.getRootNode().modificationType());
    }

    private static void assertUnmodified(final int expSize, final Collection<DataTreeCandidateNode> nodes) {
        assertEquals(expSize, nodes.size());
        nodes.forEach(node -> assertEquals(ModificationType.UNMODIFIED, node.modificationType()));
    }
}
