/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import java.util.Map.Entry;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreTreeNodesTest extends AbstractTestModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(StoreTreeNodesTest.class);

    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;
    private static final String TWO_ONE_NAME = "one";
    private static final String TWO_TWO_NAME = "two";

    private static final YangInstanceIdentifier OUTER_LIST_1_PATH = YangInstanceIdentifier.builder(
        TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID)
            .build();

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH = YangInstanceIdentifier.builder(
        TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .build();

    private static final YangInstanceIdentifier TWO_TWO_PATH = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
            .node(TestModel.INNER_LIST_QNAME)
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_TWO_NAME)
            .build();

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME)
                    .withChild(mapEntry(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_ONE_NAME))
                    .withChild(mapEntry(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_TWO_NAME))
                    .build())
                    .build();

    private RootApplyStrategy rootOper;

    @Before
    public void prepare() {
        rootOper = RootApplyStrategy.from(SchemaAwareApplyOperation.from(SCHEMA_CONTEXT,
            DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

    public NormalizedNode<?, ?> createDocumentOne() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(SCHEMA_CONTEXT.getQName()))
                .withChild(createTestContainer()).build();

    }

    @Test
    public void findNodeTestNodeFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final Optional<TreeNode> node = StoreTreeNodes.findNode(rootNode, OUTER_LIST_1_PATH);
        assertPresentAndType(node, TreeNode.class);
    }

    @Test
    public void findNodeTestNodeNotFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final YangInstanceIdentifier outerList1InvalidPath = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
                .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 3) //
                .build();
        final Optional<TreeNode> node = StoreTreeNodes.findNode(rootNode, outerList1InvalidPath);
        assertFalse(node.isPresent());
    }

    @Test
    public void findNodeCheckedTestNodeFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        TreeNode foundNode = null;
        try {
            foundNode = StoreTreeNodes.findNodeChecked(rootNode, OUTER_LIST_1_PATH);
        } catch (final IllegalArgumentException e) {
            fail("Illegal argument exception was thrown and should not have been" + e.getMessage());
        }
        assertNotNull(foundNode);
    }

    @Test
    public void findNodeCheckedTestNodeNotFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final YangInstanceIdentifier outerList1InvalidPath = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
                .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 3) //
                .build();
        try {
            StoreTreeNodes.findNodeChecked(rootNode, outerList1InvalidPath);
            fail("Illegal argument exception should have been thrown");
        } catch (final IllegalArgumentException e) {
            LOG.debug("Illegal argument exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void findClosestOrFirstMatchTestNodeExists() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final Optional<TreeNode> expectedNode = StoreTreeNodes.findNode(rootNode, TWO_TWO_PATH);
        assertPresentAndType(expectedNode, TreeNode.class);

        final Entry<YangInstanceIdentifier, TreeNode> actualNode = StoreTreeNodes.findClosest(rootNode, TWO_TWO_PATH);
        assertTreeNodeEquals(expectedNode.get(), actualNode.getValue());
    }

    @Test
    public void findClosestOrFirstMatchTestNodeDoesNotExist() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final YangInstanceIdentifier outerListInnerListPath = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
                .node(TestModel.INNER_LIST_QNAME)
                .build();
        final YangInstanceIdentifier twoTwoInvalidPath = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
                .node(TestModel.INNER_LIST_QNAME)
                .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, "three")
                .build();
        final Optional<TreeNode> expectedNode = StoreTreeNodes.findNode(rootNode, outerListInnerListPath);
        assertPresentAndType(expectedNode, TreeNode.class);

        final Entry<YangInstanceIdentifier, TreeNode> actualNode = StoreTreeNodes.findClosest(rootNode,
            twoTwoInvalidPath);
        assertTreeNodeEquals(expectedNode.get(), actualNode.getValue());
    }

    @Test
    public void getChildTestChildFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final Optional<TreeNode> node = StoreTreeNodes.getChild(Optional.ofNullable(rootNode),
                TestModel.TEST_PATH.getLastPathArgument());
        assertPresentAndType(node, TreeNode.class);
    }

    @Test
    public void getChildTestChildNotFound() {
        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNodeFactory.createTreeNode(createDocumentOne(), Version.initial()), rootOper);
        final TreeNode rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final Optional<TreeNode> node = StoreTreeNodes.getChild(Optional.ofNullable(rootNode),
                TestModel.OUTER_LIST_PATH.getLastPathArgument());
        assertFalse(node.isPresent());
    }

    private static ContainerNode createTestContainer() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                        .withChild(mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID))
                        .withChild(BAR_NODE).build()).build();
    }

    private static <T extends TreeNode> T assertPresentAndType(final Optional<TreeNode> potential,
            final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.get()));
        return type.cast(potential.get());
    }

    private static void assertTreeNodeEquals(final TreeNode expected, final TreeNode actual) {
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getSubtreeVersion(), actual.getSubtreeVersion());
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.toString(), actual.toString());
    }
}
