/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
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

    @BeforeEach
    void prepare() throws ExcludedDataSchemaNodeException {
        rootOper = RootApplyStrategy.from(SchemaAwareApplyOperation.from(SCHEMA_CONTEXT,
            DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

    public static @NonNull ContainerNode createDocumentOne() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
            .withChild(createTestContainer())
            .build();
    }

    @Test
    void findNodeTestNodeFound() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final var node = StoreTreeNodes.findNode(rootNode, OUTER_LIST_1_PATH);
        assertPresentAndType(node, TreeNode.class);
    }

    @Test
    void findNodeTestNodeNotFound() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final var outerList1InvalidPath = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
                .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 3) //
                .build();
        final var node = StoreTreeNodes.findNode(rootNode, outerList1InvalidPath);
        assertFalse(node.isPresent());
    }

    @Test
    void findNodeCheckedTestNodeFound() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        TreeNode foundNode = null;
        try {
            foundNode = StoreTreeNodes.findNodeChecked(rootNode, OUTER_LIST_1_PATH);
        } catch (final IllegalArgumentException e) {
            fail("Illegal argument exception was thrown and should not have been" + e.getMessage());
        }
        assertNotNull(foundNode);
    }

    @Test
    void findNodeCheckedTestNodeNotFound() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final var outerList1InvalidPath = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
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
    void findClosestOrFirstMatchTestNodeExists() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final var expectedNode = StoreTreeNodes.findNode(rootNode, TWO_TWO_PATH);
        assertPresentAndType(expectedNode, TreeNode.class);

        final var actualNode = StoreTreeNodes.findClosest(rootNode, TWO_TWO_PATH);
        assertTreeNodeEquals(expectedNode.orElseThrow(), actualNode.getValue());
    }

    @Test
    void findClosestOrFirstMatchTestNodeDoesNotExist() {
        final var inMemoryDataTreeSnapshot = new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial(false)), rootOper);
        final var rootNode = inMemoryDataTreeSnapshot.getRootNode();
        final var outerListInnerListPath = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
                .node(TestModel.INNER_LIST_QNAME)
                .build();
        final var twoTwoInvalidPath = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
                .node(TestModel.INNER_LIST_QNAME)
                .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, "three")
                .build();
        final var expectedNode = StoreTreeNodes.findNode(rootNode, outerListInnerListPath);
        assertPresentAndType(expectedNode, TreeNode.class);

        final var actualNode = StoreTreeNodes.findClosest(rootNode,
            twoTwoInvalidPath);
        assertTreeNodeEquals(expectedNode.orElseThrow(), actualNode.getValue());
    }

    private static ContainerNode createTestContainer() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                .withChild(mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID))
                .withChild(BAR_NODE)
                .build())
            .build();
    }

    private static <T extends TreeNode> T assertPresentAndType(final Optional<? extends TreeNode> potential,
            final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.orElseThrow()));
        return type.cast(potential.orElseThrow());
    }

    private static void assertTreeNodeEquals(final TreeNode expected, final TreeNode actual) {
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getSubtreeVersion(), actual.getSubtreeVersion());
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.toString(), actual.toString());
    }
}
