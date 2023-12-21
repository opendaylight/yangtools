/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;

class BuilderTest {
    private static final QName ROOT_CONTAINER = QName.create("test.namespace.builder.test", "2016-01-01",
        "root-container");
    private static final QName LIST_MAIN = QName.create(ROOT_CONTAINER, "list-ordered-by-user-with-key");
    private static final QName LEAF_LIST_MAIN = QName.create(ROOT_CONTAINER, "leaf-list-ordered-by-user");
    private static final QName LIST_MAIN_CHILD_QNAME_1 = QName.create(ROOT_CONTAINER, "leaf-a");
    private static final NodeIdentifier NODE_IDENTIFIER_LIST = NodeIdentifier.create(LIST_MAIN);
    private static final NodeIdentifier NODE_IDENTIFIER_LEAF_LIST = NodeIdentifier.create(LEAF_LIST_MAIN);
    private static final NodeIdentifier NODE_IDENTIFIER_LEAF = NodeIdentifier.create(LIST_MAIN_CHILD_QNAME_1);
    private static final MapEntryNode LIST_MAIN_CHILD_1 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1,
            1);
    private static final MapEntryNode LIST_MAIN_CHILD_2 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1,
            2);
    private static final MapEntryNode LIST_MAIN_CHILD_3 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1,
            3);
    private static final int SIZE = 3;
    private static final NodeWithValue<String> BAR_PATH = new NodeWithValue<>(LEAF_LIST_MAIN, "bar");
    private static final LeafSetEntryNode<String> LEAF_SET_ENTRY_NODE =
            new ImmutableLeafSetEntryNodeBuilder<String>().withNodeIdentifier(BAR_PATH).withValue("bar").build();

    @Test
    void immutableOrderedMapBuilderTest() {
        final var mapEntryPath = NodeIdentifierWithPredicates.of(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1);
        final var orderedMapNodeCreateNull = Builders.orderedMapBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(List.of(LIST_MAIN_CHILD_3))
                .build();
        final var orderedMapNodeCreateSize = Builders.orderedMapBuilder(SIZE)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var orderedMapNodeCreateNode = ImmutableUserMapNodeBuilder.create(orderedMapNodeCreateNull)
                .removeChild(mapEntryPath)
                .build();
        final var orderedMapNodeSchemaAware = Builders.orderedMapBuilder()
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
        final var orderedLeafSet = Builders.<String>orderedLeafSetBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .withChild(LEAF_SET_ENTRY_NODE)
                .withChildValue("baz")
                .removeChild(BAR_PATH)
                .build();
        final var orderedMapNodeSchemaAware = Builders.orderedLeafSetBuilder()
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .withChildValue("baz")
            .build();

        assertNotNull(Builders.anyXmlBuilder());
        assertEquals(1, orderedLeafSet.size());
        assertEquals("baz", orderedLeafSet.childAt(0).body());
        assertNull(orderedLeafSet.childByArg(BAR_PATH));
        assertEquals(1, orderedLeafSet.size());
        assertEquals(1, orderedMapNodeSchemaAware.size());
    }

    @Test
    void immutableMapNodeBuilderTest() {
        final var mapNode = Builders.mapBuilder(1)
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .withValue(List.of(LIST_MAIN_CHILD_3))
            .build();
        assertNotNull(Builders.mapBuilder(mapNode));
    }

    @Test
    void immutableUnkeyedListEntryNodeBuilderTest() {
        final var unkeyedListEntryNode = Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeSize = Builders.unkeyedListEntryBuilder(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeNode = ImmutableUnkeyedListEntryNodeBuilder.create(unkeyedListEntryNode).build();
        assertEquals(unkeyedListEntryNode.name(), unkeyedListEntryNodeSize.name());
        assertEquals(unkeyedListEntryNodeSize.name(), unkeyedListEntryNodeNode.name());
    }

    @Test
    void immutableUnkeyedListNodeBuilderTest() {
        final var unkeyedListEntryNode = Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF)
                .build();
        final var immutableUnkeyedListNodeBuilder = Builders.unkeyedListBuilder();
        final var unkeyedListNode = immutableUnkeyedListNodeBuilder
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .addChild(unkeyedListEntryNode)
                .build();
        final var unkeyedListNodeSize = Builders.unkeyedListBuilder(1)
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
        final var choiceNode = Builders.choiceBuilder(1).withNodeIdentifier(NODE_IDENTIFIER_LIST).build();
        final var choiceNodeCreated = ImmutableChoiceNodeBuilder.create(choiceNode).build();
        assertEquals(choiceNode, choiceNodeCreated);
    }

    @Test
    void immutableContainerNodeBuilderExceptionTest() {
        final var containerNode = Builders.containerBuilder(1).withNodeIdentifier(NODE_IDENTIFIER_LIST).build();
        assertNotNull(containerNode);
    }

    @Test
    void immutableLeafSetNodeBuilderExceptionTest() {
        final var leafSetNode = Builders.leafSetBuilder(1).withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST).build();
        assertNotNull(leafSetNode);
    }

    @Test
    void immutableMapEntryNodeBuilderExceptionTest() {
        final var builder = Builders.mapEntryBuilder(1);
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void immutableUnkeyedListNodeBuilderExceptionTest() {
        final var builder = Builders.unkeyedListBuilder().withNodeIdentifier(NODE_IDENTIFIER_LEAF);
        assertThrows(UnsupportedOperationException.class, () -> builder.removeChild(NODE_IDENTIFIER_LIST));
    }

    private static UserMapNode getImmutableUserMapNode() {
        return Builders.orderedMapBuilder()
            .withNodeIdentifier(NODE_IDENTIFIER_LIST)
            .withChild(LIST_MAIN_CHILD_1)
            .build();
    }
}
