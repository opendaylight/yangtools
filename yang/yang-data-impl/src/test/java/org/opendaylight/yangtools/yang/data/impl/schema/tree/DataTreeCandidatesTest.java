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

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidatesTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidates.class);

    private static final SchemaContext SCHEMA_CONTEXT = TestModel.createTestContext();

    private DataTree dataTree;

    @Before
    public void setUp() throws Exception {
        dataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        dataTree.setSchemaContext(SCHEMA_CONTEXT);

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
                        .build())
                .build();

        final InMemoryDataTreeModification modification = (InMemoryDataTreeModification) dataTree.takeSnapshot().newModification();
        final DataTreeModificationCursor cursor = modification.createCursor(YangInstanceIdentifier.EMPTY);
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        modification.ready();

        dataTree.validate(modification);
        final DataTreeCandidate candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);
    }

    @Test
    public void testRootedCandidate() throws Exception {
        final DataTree innerDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL, TestModel.INNER_CONTAINER_PATH);
        innerDataTree.setSchemaContext(SCHEMA_CONTEXT);

        final LeafNode<String> leaf = ImmutableLeafNodeBuilder.<String>create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.VALUE_QNAME))
                .withValue("testing-value")
                .build();

        final InMemoryDataTreeModification modification = (InMemoryDataTreeModification) innerDataTree.takeSnapshot().newModification();
        modification.write(TestModel.VALUE_PATH, leaf);

        modification.ready();
        dataTree.validate(modification);
        final DataTreeCandidate candidate = dataTree.prepare(modification);
        dataTree.commit(candidate);

        final DataTreeModification newModification = dataTree.takeSnapshot().newModification();
        final DataTreeCandidate newCandidate = DataTreeCandidates.newDataTreeCandidate(TestModel.INNER_CONTAINER_PATH, candidate.getRootNode());

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
}