/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidatesTest extends AbstractTestModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidatesTest.class);

    private DataTree dataTree;

    @Before
    public void setUp() throws Exception {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(ImmutableContainerNodeBuilder.create()
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

        final LeafNode<String> leaf = ImmutableLeafNodeBuilder.<String>create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.VALUE_QNAME))
                .withValue("testing-value")
                .build();

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
        assertEquals(YangInstanceIdentifier.EMPTY, candidate.getRootPath());
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
        assertEquals(YangInstanceIdentifier.EMPTY, candidate.getRootPath());
        final DataTreeCandidateNode node = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, node.getModificationType());

        // 'non-presence' and 'test'
        assertUnmodified(2, node.getChildNodes());
    }

    private static void assertUnmodified(final int expSize, final Collection<DataTreeCandidateNode> nodes) {
        assertEquals(expSize, nodes.size());
        nodes.forEach(node -> assertEquals(ModificationType.UNMODIFIED, node.getModificationType()));
    }
}
