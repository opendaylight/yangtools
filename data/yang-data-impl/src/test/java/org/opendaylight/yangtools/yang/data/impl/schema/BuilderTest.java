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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

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
            ImmutableLeafSetEntryNodeBuilder.<String>create()
            .withNodeIdentifier(BAR_PATH)
            .withValue("bar")
            .build();
    private ListSchemaNode list;
    private LeafListSchemaNode leafList;

    @BeforeEach
    void setup() throws URISyntaxException {
        final var schema = YangParserTestUtils.parseYang("""
            module immutable-ordered-map-node {
              yang-version 1;
              namespace "test.namespace.builder.test";
              prefix "iomn";

              revision "2016-01-01" {
                description "Initial revision.";
              }

              container root-container {
                list list-ordered-by-user-with-key {
                  key "leaf-a";
                  ordered-by "user";
                  leaf leaf-a {
                    type string;
                  }
                }
                leaf-list leaf-list-ordered-by-user {
                  ordered-by "user";
                  type string;
                }
              }
            }""");
        final var module = schema.getModules().iterator().next();
        final var root = module.getDataChildByName(ROOT_CONTAINER);
        list = (ListSchemaNode)((ContainerSchemaNode) root).getDataChildByName(LIST_MAIN);
        leafList = (LeafListSchemaNode)((ContainerSchemaNode) root).getDataChildByName(LEAF_LIST_MAIN);
    }

    @Test
    void immutableOrderedMapBuilderTest() {
        final var mapEntryNodeColl = new LinkedList<MapEntryNode>();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);
        final var keys = new HashMap<QName, Object>();
        keys.put(LIST_MAIN_CHILD_QNAME_1, 1);
        final var mapEntryPath = NodeIdentifierWithPredicates.of(LIST_MAIN, keys);
        final var orderedMapNodeCreateNull = ImmutableUserMapNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(mapEntryNodeColl)
                .build();
        final var orderedMapNodeCreateSize = ImmutableUserMapNodeBuilder.create(SIZE)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var orderedMapNodeCreateNode = ImmutableUserMapNodeBuilder.create(orderedMapNodeCreateNull)
                .removeChild(mapEntryPath)
                .build();
        final var orderedMapNodeSchemaAware = ImmutableUserMapNodeBuilder.create()
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
        final var orderedLeafSet = ImmutableUserLeafSetNodeBuilder.<String>create()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .withChild(LEAF_SET_ENTRY_NODE)
                .withChildValue("baz")
                .removeChild(BAR_PATH)
                .build();
        final var mapEntryNodeColl = new LinkedList<>();
        mapEntryNodeColl.add(orderedLeafSet);
        final var leafSetCollection = (UnmodifiableCollection<?>)orderedLeafSet.body();
        final var orderedMapNodeSchemaAware = ImmutableUserLeafSetNodeBuilder.create()
            .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
            .withChildValue("baz")
            .build();
        final var SchemaAwareleafSetCollection =
                (UnmodifiableCollection<?>) orderedMapNodeSchemaAware.body();

        assertNotNull(Builders.anyXmlBuilder());
        assertEquals(1, ((UserLeafSetNode<?>)orderedLeafSet).size());
        assertEquals("baz", orderedLeafSet.childAt(0).body());
        assertNull(orderedLeafSet.childByArg(BAR_PATH));
        assertEquals(1, leafSetCollection.size());
        assertEquals(1, SchemaAwareleafSetCollection.size());
    }

    @Test
    void immutableMapNodeBuilderTest() {
        final var mapEntryNodeColl = new LinkedList<MapEntryNode>();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);
        final var collectionNodeBuilder =
            ImmutableMapNodeBuilder.create(1);
        assertNotNull(collectionNodeBuilder);
        collectionNodeBuilder.withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST);
        collectionNodeBuilder.withValue(mapEntryNodeColl);
        final var mapNode = collectionNodeBuilder.build();
        assertNotNull(Builders.mapBuilder(mapNode));
    }

    @Test
    void immutableUnkeyedListEntryNodeBuilderTest() {
        final var unkeyedListEntryNode = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeSize = ImmutableUnkeyedListEntryNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var unkeyedListEntryNodeNode = ImmutableUnkeyedListEntryNodeBuilder
                .create(unkeyedListEntryNode).build();
        assertEquals(unkeyedListEntryNode.name(), unkeyedListEntryNodeSize.name());
        assertEquals(unkeyedListEntryNodeSize.name(), unkeyedListEntryNodeNode.name());
    }

    @Test
    void immutableUnkeyedListNodeBuilderTest() {
        final var unkeyedListEntryNode = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF)
                .build();
        final var immutableUnkeyedListNodeBuilder = (ImmutableUnkeyedListNodeBuilder)
                ImmutableUnkeyedListNodeBuilder.create();
        final var unkeyedListNode = immutableUnkeyedListNodeBuilder
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .addChild(unkeyedListEntryNode)
                .build();
        final var unkeyedListNodeSize = ImmutableUnkeyedListNodeBuilder.create(1)
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
        final var choiceNode = ImmutableChoiceNodeBuilder.create(1).withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final var choiceNodeCreated = ImmutableChoiceNodeBuilder.create(choiceNode).build();
        assertEquals(choiceNodeCreated.name(), choiceNode.name());
    }

    @Test
    void immutableContainerNodeBuilderExceptionTest() {
        final var immutableContainerNode = ImmutableContainerNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        assertNotNull(immutableContainerNode);
    }

    @Test
    void immutableLeafSetNodeBuilderExceptionTest() {
        final var leafSetNode = ImmutableLeafSetNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .build();
        assertNotNull(leafSetNode);
    }

    @Test
    void immutableMapEntryNodeBuilderExceptionTest() {
        final var builder = ImmutableMapEntryNodeBuilder.create(1);
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void immutableUnkeyedListNodeBuilderExceptionTest() {
        final var builder = ImmutableUnkeyedListNodeBuilder.create().withNodeIdentifier(NODE_IDENTIFIER_LEAF);
        assertThrows(UnsupportedOperationException.class, () -> builder.removeChild(NODE_IDENTIFIER_LIST));
    }

    private static SystemMapNode getImmutableMapNode() {
        return ImmutableMapNodeBuilder.create()
            .withNodeIdentifier(NODE_IDENTIFIER_LIST)
            .withChild(LIST_MAIN_CHILD_1)
            .build();
    }

    private static UserMapNode getImmutableUserMapNode() {
        return ImmutableUserMapNodeBuilder.create()
            .withNodeIdentifier(NODE_IDENTIFIER_LIST)
            .withChild(LIST_MAIN_CHILD_1)
            .build();
    }
}
