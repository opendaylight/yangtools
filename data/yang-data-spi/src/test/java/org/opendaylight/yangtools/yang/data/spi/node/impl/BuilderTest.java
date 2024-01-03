/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class BuilderTest {
    private static final BuilderFactory BUILDER_FACTORY = ImmutableNodes.builderFactory();
    private static final QName ROOT_CONTAINER = QName.create("test.namespace.builder.test", "2016-01-01",
        "root-container");
    private static final QName LIST_MAIN = QName.create(ROOT_CONTAINER, "list-ordered-by-user-with-key");
    private static final QName LEAF_LIST_MAIN = QName.create(ROOT_CONTAINER, "leaf-list-ordered-by-user");
    private static final QName LIST_MAIN_CHILD_QNAME_1 = QName.create(ROOT_CONTAINER, "leaf-a");
    private static final NodeIdentifier NODE_IDENTIFIER_LIST = NodeIdentifier.create(LIST_MAIN);
    private static final NodeIdentifier NODE_IDENTIFIER_LEAF_LIST = NodeIdentifier.create(LEAF_LIST_MAIN);
    private static final NodeIdentifier NODE_IDENTIFIER_LEAF = NodeIdentifier.create(LIST_MAIN_CHILD_QNAME_1);
    private static final MapEntryNode LIST_MAIN_CHILD_1 = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1))
        .withChild(ImmutableNodes.leafNode(LIST_MAIN_CHILD_QNAME_1, 1))
        .build();
    private static final MapEntryNode LIST_MAIN_CHILD_2 = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 2))
        .withChild(ImmutableNodes.leafNode(LIST_MAIN_CHILD_QNAME_1, 2))
        .build();
    private static final MapEntryNode LIST_MAIN_CHILD_3 = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 3))
        .withChild(ImmutableNodes.leafNode(LIST_MAIN_CHILD_QNAME_1, 3))
        .build();
    private static final int SIZE = 3;
    private static final NodeWithValue<String> BAR_PATH = new NodeWithValue<>(LEAF_LIST_MAIN, "bar");
    private static final LeafSetEntryNode<String> LEAF_SET_ENTRY_NODE =
            new ImmutableLeafSetEntryNodeBuilder<String>().withNodeIdentifier(BAR_PATH).withValue("bar").build();

    @Test
    void immutableOrderedMapBuilderTest() {
        final var mapEntryPath = NodeIdentifierWithPredicates.of(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1);
        final var orderedMapNodeCreateNull = ImmutableNodes.newUserMapBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(List.of(LIST_MAIN_CHILD_3))
                .build();
        final var orderedMapNodeCreateSize = ImmutableNodes.builderFactory().newUserMapBuilder(SIZE)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var orderedMapNodeCreateNode = ImmutableUserMapNodeBuilder.create(orderedMapNodeCreateNull)
                .removeChild(mapEntryPath)
                .build();
        final var orderedMapNodeSchemaAware = ImmutableNodes.newUserMapBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .withChild(LIST_MAIN_CHILD_1)
                .build();
        final var orderedMapNodeSchemaAwareMapNodeConst =
                ImmutableUserMapNodeBuilder.create(getImmutableUserMapNode())
                .build();

        assertEquals(SIZE, orderedMapNodeCreateNull.size());
        assertEquals(orderedMapNodeCreateNode.size(), orderedMapNodeCreateNull.size() - 1);
        assertEquals(NODE_IDENTIFIER_LIST, orderedMapNodeCreateSize.name());
        assertEquals(LIST_MAIN_CHILD_1, orderedMapNodeCreateNull.childAt(0));
        assertEquals(SIZE, orderedMapNodeCreateNull.size());
        assertEquals(orderedMapNodeSchemaAware.childAt(0), orderedMapNodeSchemaAwareMapNodeConst.childAt(0));
    }

    @Test
    void immutableUserLeafSetNodeBuilderTest() {
        final var orderedLeafSet = ImmutableNodes.<String>newUserLeafSetBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .withChild(LEAF_SET_ENTRY_NODE)
                .withChildValue("baz")
                .removeChild(BAR_PATH)
                .build();
        final var orderedMapNodeSchemaAware = ImmutableNodes.newUserLeafSetBuilder()
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .withChildValue("baz")
            .build();

        assertNotNull(ImmutableNodes.newAnyxmlBuilder(DOMSource.class));
        assertEquals(1, orderedLeafSet.size());
        assertEquals("baz", orderedLeafSet.childAt(0).body());
        assertNull(orderedLeafSet.childByArg(BAR_PATH));
        assertEquals(1, orderedLeafSet.size());
        assertEquals(1, orderedMapNodeSchemaAware.size());
    }

    @Test
    void immutableMapNodeBuilderTest() {
        final var mapNode = BUILDER_FACTORY.newSystemMapBuilder(1)
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .withValue(List.of(LIST_MAIN_CHILD_3))
            .build();
        assertNotNull(BUILDER_FACTORY.newSystemMapBuilder(mapNode));
    }

    @Test
    void immutableUnkeyedListEntryNodeBuilderTest() {
        final var unkeyedListEntryNode = ImmutableNodes.newUnkeyedListEntryBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeSize = BUILDER_FACTORY.newUnkeyedListEntryBuilder(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeNode = ImmutableUnkeyedListEntryNodeBuilder.create(unkeyedListEntryNode).build();
        assertEquals(unkeyedListEntryNode.name(), unkeyedListEntryNodeSize.name());
        assertEquals(unkeyedListEntryNodeSize.name(), unkeyedListEntryNodeNode.name());
    }

    @Test
    void immutableUnkeyedListNodeBuilderTest() {
        final var unkeyedListEntryNode = ImmutableNodes.newUnkeyedListEntryBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF)
                .build();
        final var immutableUnkeyedListNodeBuilder = ImmutableNodes.newUnkeyedListBuilder();
        final var unkeyedListNode = immutableUnkeyedListNodeBuilder
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .addChild(unkeyedListEntryNode)
                .build();
        final var unkeyedListNodeSize = BUILDER_FACTORY.newUnkeyedListBuilder(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .build();
        final var unkeyedListNodeCreated = ImmutableUnkeyedListNodeBuilder.create(unkeyedListNode)
                .build();

        assertThrows(IndexOutOfBoundsException.class, () -> unkeyedListNodeSize.childAt(1));

        assertNotNull(unkeyedListNodeSize.body());
        assertEquals(unkeyedListEntryNode, unkeyedListNodeCreated.childAt(0));
        assertEquals(unkeyedListNode.name(), unkeyedListNodeSize.name());
        assertNotNull(unkeyedListNodeCreated);
    }

    @Test
    void immutableChoiceNodeBuilderTest() {
        final var choiceNode = BUILDER_FACTORY.newChoiceBuilder(1).withNodeIdentifier(NODE_IDENTIFIER_LIST).build();
        final var choiceNodeCreated = ImmutableChoiceNodeBuilder.create(choiceNode).build();
        assertEquals(choiceNode, choiceNodeCreated);
    }

    @Test
    void immutableContainerNodeBuilderExceptionTest() {
        final var containerNode = BUILDER_FACTORY.newContainerBuilder(1)
            .withNodeIdentifier(NODE_IDENTIFIER_LIST)
            .build();
        assertNotNull(containerNode);
    }

    @Test
    void immutableLeafSetNodeBuilderExceptionTest() {
        final var leafSetNode = BUILDER_FACTORY.newSystemLeafSetBuilder(1)
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .build();
        assertNotNull(leafSetNode);
    }

    @Test
    void immutableMapEntryNodeBuilderExceptionTest() {
        final var builder = BUILDER_FACTORY.newMapEntryBuilder(1);
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void immutableUnkeyedListNodeBuilderExceptionTest() {
        final var builder = ImmutableNodes.newUnkeyedListBuilder().withNodeIdentifier(NODE_IDENTIFIER_LEAF);
        assertThrows(UnsupportedOperationException.class, () -> builder.removeChild(NODE_IDENTIFIER_LIST));
    }

    private static UserMapNode getImmutableUserMapNode() {
        return ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(NODE_IDENTIFIER_LIST)
            .withChild(LIST_MAIN_CHILD_1)
            .build();
    }
}
