/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidatesTest extends AbstractTestModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidatesTest.class);

    private DataTree dataTree;

    @Before
    public void setUp() throws Exception {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

        final ContainerNode testContainer = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
                .build())
            .build();

        final InMemoryDataTreeModification modification = (InMemoryDataTreeModification) dataTree.takeSnapshot()
                .newModification();
        final DataTreeModificationCursor cursor = modification.openCursor();
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        modification.ready();

        dataTree.validate(modification);
        final DataTreeCandidate candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);
    }

    @Test
    public void testRootedCandidate() throws DataValidationFailedException {
        final DataTree innerDataTree = new InMemoryDataTreeFactory().create(
            new DataTreeConfiguration.Builder(TreeType.OPERATIONAL)
            .setMandatoryNodesValidation(true)
            .setRootPath(TestModel.INNER_CONTAINER_PATH)
            .setUniqueIndexes(true).build(), SCHEMA_CONTEXT);

        final LeafNode<String> leaf = ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "testing-value");

        final DataTreeModification modification = innerDataTree.takeSnapshot().newModification();
        modification.write(TestModel.VALUE_PATH, leaf);

        modification.ready();
        dataTree.validate(modification);
        final DataTreeCandidate candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);

        final DataTreeModification newModification = dataTree.takeSnapshot().newModification();
        final DataTreeCandidate newCandidate = DataTreeCandidates.newDataTreeCandidate(TestModel.INNER_CONTAINER_PATH,
            candidate.getRootNode());

        try {
            // lets see if getting the identifier of the root node throws an exception
            newCandidate.getRootNode().getIdentifier();
            fail();
        } catch (IllegalStateException e) {
            LOG.debug("Cannot get identifier of root node candidate which is correct", e);
        }

        // lets see if we can apply this rooted candidate to a new dataTree
        DataTreeCandidates.applyToModification(newModification,
                newCandidate);

        final LeafNode<?> readLeaf = (LeafNode<?>) newModification.readNode(TestModel.INNER_VALUE_PATH).get();
        assertEquals(readLeaf, leaf);
    }

    @Test
    public void testEmptyMergeOnContainer() throws DataValidationFailedException {
        DataTreeModification modification = dataTree.takeSnapshot().newModification();
        modification.merge(TestModel.NON_PRESENCE_PATH, ImmutableNodes.containerNode(TestModel.NON_PRESENCE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        // The entire transaction needs to fizzle to a no-op
        DataTreeCandidate candidate = dataTree.prepare(modification);
        DataTreeCandidateNode node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.getModificationType());

        // 'test'
        assertUnmodified(1, node.getChildNodes());
    }

    @Test
    public void testEmptyWriteOnContainer() throws DataValidationFailedException {
        DataTreeModification modification = dataTree.takeSnapshot().newModification();
        modification.write(TestModel.NON_PRESENCE_PATH, ImmutableNodes.containerNode(TestModel.NON_PRESENCE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        // The entire transaction needs to fizzle to a no-op
        DataTreeCandidate candidate = dataTree.prepare(modification);
        DataTreeCandidateNode node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.getModificationType());

        // 'test'
        assertUnmodified(1, node.getChildNodes());
    }

    @Test
    public void testEmptyMergesOnDeleted() throws DataValidationFailedException {
        DataTreeModification modification = dataTree.takeSnapshot().newModification();
        modification.delete(TestModel.NON_PRESENCE_PATH);
        modification.merge(TestModel.DEEP_CHOICE_PATH, ImmutableNodes.choiceNode(TestModel.DEEP_CHOICE_QNAME));
        modification.ready();
        dataTree.validate(modification);

        final DataTreeCandidate candidate = dataTree.prepare(modification);
        assertEquals(YangInstanceIdentifier.empty(), candidate.getRootPath());
        final DataTreeCandidateNode node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.getModificationType());

        // 'test'
        assertUnmodified(1, node.getChildNodes());
    }

    @Test
    public void testEmptyMergesOnExisting() throws DataValidationFailedException {
        // Make sure 'non-presence' is present
        DataTreeModification modification = dataTree.takeSnapshot().newModification();
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
        final DataTreeCandidate candidate = dataTree.prepare(modification);
        assertEquals(YangInstanceIdentifier.empty(), candidate.getRootPath());
        final DataTreeCandidateNode node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.getModificationType());

        // 'non-presence' and 'test'
        assertUnmodified(2, node.getChildNodes());
    }

    @Test
    public void testAggregateWithoutChanges() throws DataValidationFailedException {
        DataTreeModification modification1 = dataTree.takeSnapshot().newModification();
        modification1.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value1"));
        modification1.ready();
        dataTree.validate(modification1);
        DataTreeCandidate candidate1 = dataTree.prepare(modification1);
        dataTree.commit(candidate1);

        DataTreeModification modification2 = dataTree.takeSnapshot().newModification();
        modification2.delete(TestModel.INNER_CONTAINER_PATH);
        modification2.ready();
        dataTree.validate(modification2);
        DataTreeCandidate candidate2 = dataTree.prepare(modification2);
        dataTree.commit(candidate2);

        DataTreeCandidate aggregateCandidate = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        assertEquals(ModificationType.UNMODIFIED,aggregateCandidate.getRootNode().getModificationType());
    }

    @Test
    public void testAggregate() throws DataValidationFailedException {
        DataTreeModification modification = dataTree.takeSnapshot().newModification();
        modification.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value1"));
        modification.ready();
        dataTree.validate(modification);
        DataTreeCandidate candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);

        DataTreeModification modification1 = dataTree.takeSnapshot().newModification();
        modification1.delete(TestModel.INNER_CONTAINER_PATH);
        modification1.ready();
        dataTree.validate(modification1);
        DataTreeCandidate candidate1 = dataTree.prepare(modification1);
        dataTree.commit(candidate1);

        DataTreeModification modification2 = dataTree.takeSnapshot().newModification();
        modification2.write(
                TestModel.INNER_CONTAINER_PATH.node(QName.create(TestModel.INNER_CONTAINER_QNAME,"value")),
                ImmutableNodes.leafNode(QName.create(TestModel.INNER_CONTAINER_QNAME,"value"),"value2"));
        modification2.ready();
        dataTree.validate(modification2);
        DataTreeCandidate candidate2 = dataTree.prepare(modification2);
        dataTree.commit(candidate2);

        DataTreeCandidate aggregateCandidate = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED,aggregateCandidate.getRootNode().getModificationType());
    }

    private static void assertUnmodified(final int expSize, final Collection<DataTreeCandidateNode> nodes) {
        assertEquals(expSize, nodes.size());
        nodes.forEach(node -> assertEquals(ModificationType.UNMODIFIED, node.getModificationType()));
    }
}
