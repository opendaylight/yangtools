/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableYangModeledAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BuilderTest {
    private static final QName ROOT_CONTAINER = QName.create("test.namespace.builder.test", "2016-01-01", "root-container");
    private static final QName LIST_MAIN = QName.create(ROOT_CONTAINER, "list-ordered-by-user-with-key");
    private static final QName LEAF_LIST_MAIN = QName.create(ROOT_CONTAINER, "leaf-list-ordered-by-user");
    private static final QName LIST_MAIN_CHILD_QNAME_1 = QName.create(ROOT_CONTAINER, "leaf-a");
    private static final YangInstanceIdentifier.NodeIdentifier NODE_IDENTIFIER_LIST = YangInstanceIdentifier.NodeIdentifier
            .create(LIST_MAIN);
    private static final YangInstanceIdentifier.NodeIdentifier NODE_IDENTIFIER_LEAF_LIST = YangInstanceIdentifier.NodeIdentifier
            .create(LEAF_LIST_MAIN);
    private static final YangInstanceIdentifier.NodeIdentifier NODE_IDENTIFIER_LEAF = YangInstanceIdentifier
            .NodeIdentifier.create(LIST_MAIN_CHILD_QNAME_1);
    private static final MapEntryNode LIST_MAIN_CHILD_1 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1);
    private static final MapEntryNode LIST_MAIN_CHILD_2 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 2);
    private static final MapEntryNode LIST_MAIN_CHILD_3 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 3);
    private static final int SIZE = 3;
    private static final NodeWithValue<String> BAR_PATH = new NodeWithValue<>(LEAF_LIST_MAIN, "bar");
    private static final LeafSetEntryNode LEAF_SET_ENTRY_NODE = ImmutableLeafSetEntryNodeBuilder.create()
            .withNodeIdentifier(BAR_PATH)
            .withValue("bar")
            .build();
    private ListSchemaNode list;
    private LeafListSchemaNode leafList;

    @Before
    public void setup() throws FileNotFoundException, ReactorException, URISyntaxException {
        final File leafRefTestYang = new File(getClass().getResource("/builder-test/immutable-ordered-map-node.yang")
                .toURI());
        final SchemaContext schema = YangParserTestUtils.parseYangSources(leafRefTestYang);
        final Module module = schema.getModules().iterator().next();
        final DataSchemaNode root = module.getDataChildByName(ROOT_CONTAINER);
        list = (ListSchemaNode)((ContainerEffectiveStatementImpl) root).getDataChildByName(LIST_MAIN);
        leafList = (LeafListSchemaNode)((ContainerEffectiveStatementImpl) root).getDataChildByName(LEAF_LIST_MAIN);
    }

    @Test
    public void immutableOrderedMapBuilderTest() {
        final LinkedList<MapEntryNode> mapEntryNodeColl = new LinkedList<>();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);
        final Map<QName, Object> keys = new HashMap<>();
        keys.put(LIST_MAIN_CHILD_QNAME_1, 1);
        final YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(LIST_MAIN, keys);
        final OrderedMapNode orderedMapNodeCreateNull = ImmutableOrderedMapNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(mapEntryNodeColl)
                .build();
        final OrderedMapNode orderedMapNodeCreateSize = ImmutableOrderedMapNodeBuilder.create(SIZE)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final OrderedMapNode orderedMapNodeCreateNode = ImmutableOrderedMapNodeBuilder.create(orderedMapNodeCreateNull)
                .removeChild(mapEntryPath)
                .build();
        final OrderedMapNode orderedMapNodeSchemaAware = ImmutableOrderedMapNodeSchemaAwareBuilder.create(list)
                .withChild(LIST_MAIN_CHILD_1)
                .build();
        final OrderedMapNode orderedMapNodeSchemaAwareMapNodeConst = ImmutableOrderedMapNodeSchemaAwareBuilder.create
                (list, getImmutableOrderedMapNode())
                .build();

        assertNotNull(Builders.orderedMapBuilder(list));
        assertEquals(SIZE, orderedMapNodeCreateNull.getSize());
        assertEquals(orderedMapNodeCreateNode.getSize(), orderedMapNodeCreateNull.getSize() - 1);
        assertEquals(NODE_IDENTIFIER_LIST, orderedMapNodeCreateSize.getIdentifier());
        assertEquals(LIST_MAIN_CHILD_1, orderedMapNodeCreateNull.getChild(0));
        assertEquals(SIZE, orderedMapNodeCreateNull.getValue().size());
        assertEquals(orderedMapNodeSchemaAware.getChild(0), orderedMapNodeSchemaAwareMapNodeConst.getChild(0));
    }

    @Test
    public void immutableOrderedLeafSetNodeBuilderTest() {
        final NormalizedNode<?, ?> orderedLeafSet = ImmutableOrderedLeafSetNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .withChild(LEAF_SET_ENTRY_NODE)
                .withChildValue("baz")
                .removeChild(BAR_PATH)
                .build();
        final LinkedList<LeafSetNode<?>> mapEntryNodeColl = new LinkedList<>();
        mapEntryNodeColl.add((LeafSetNode<?>)orderedLeafSet);
        final Collection<?> leafSetCollection = orderedLeafSet.getValue();
        final NormalizedNode<?, ?> orderedMapNodeSchemaAware = ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(leafList)
                .withChildValue("baz")
                .build();
        final Collection<?> SchemaAwareleafSetCollection = orderedMapNodeSchemaAware.getValue();
        final NormalizedNode<?, ?> orderedLeafSetShemaAware = ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(leafList,
                (LeafSetNode<?>)orderedLeafSet)
                .build();

        assertNotNull(Builders.orderedLeafSetBuilder(leafList));
        assertNotNull(Builders.anyXmlBuilder());
        assertNotNull(orderedLeafSetShemaAware);
        assertEquals(1, ((OrderedLeafSetNode<?>)orderedLeafSet).getSize());
        assertEquals("baz", ((OrderedLeafSetNode<?>)orderedLeafSet).getChild(0).getValue());
        assertNotNull(((OrderedLeafSetNode<?>)orderedLeafSet).getChild(BAR_PATH));
        assertEquals(1, leafSetCollection.size());
        assertEquals(1, SchemaAwareleafSetCollection.size());
    }

    @Test
    public void immutableMapNodeBuilderTest() {
        final LinkedList<MapEntryNode> mapEntryNodeColl = new LinkedList<>();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);
        final CollectionNodeBuilder<MapEntryNode, MapNode> collectionNodeBuilder = ImmutableMapNodeBuilder.create(1);
        assertNotNull(collectionNodeBuilder);
        collectionNodeBuilder.withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST);
        collectionNodeBuilder.withValue(mapEntryNodeColl);
        final MapNode mapNode = collectionNodeBuilder.build();
        final MapNode mapNodeSchemaAware = ImmutableMapNodeSchemaAwareBuilder.create(list, getImmutableMapNode()).build();
        assertNotNull(mapNodeSchemaAware);
        assertNotNull(Builders.mapBuilder(mapNode));
    }

    @Test
    public void immutableUnkeyedListEntryNodeBuilderTest() {
        final UnkeyedListEntryNode unkeyedListEntryNode = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final UnkeyedListEntryNode unkeyedListEntryNodeSize = ImmutableUnkeyedListEntryNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final UnkeyedListEntryNode unkeyedListEntryNodeNode = ImmutableUnkeyedListEntryNodeBuilder.create(unkeyedListEntryNode)
                .build();
        assertEquals(unkeyedListEntryNode.getNodeType().getLocalName(), unkeyedListEntryNodeSize.getNodeType()
                .getLocalName());
        assertEquals(unkeyedListEntryNodeSize.getNodeType().getLocalName(), unkeyedListEntryNodeNode.getNodeType()
                .getLocalName());
    }

    @Test
    public void immutableUnkeyedListNodeBuilderTest() {
        final UnkeyedListEntryNode unkeyedListEntryNode = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF)
                .build();
        final ImmutableUnkeyedListNodeBuilder immutableUnkeyedListNodeBuilder = (ImmutableUnkeyedListNodeBuilder)
                ImmutableUnkeyedListNodeBuilder.create();
        final UnkeyedListNode unkeyedListNode = immutableUnkeyedListNodeBuilder.withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .addChild(unkeyedListEntryNode)
                .build();
        final UnkeyedListNode unkeyedListNodeSize = ImmutableUnkeyedListNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .build();
        final UnkeyedListNode unkeyedListNodeCreated = ImmutableUnkeyedListNodeBuilder.create(unkeyedListNode)
                .build();
        try {
            unkeyedListNodeSize.getChild(1);
        } catch (IndexOutOfBoundsException e) {
        }

        assertNotNull(unkeyedListNodeSize.getValue());
        assertEquals(unkeyedListEntryNode, unkeyedListNodeCreated.getChild(0));
        assertEquals(unkeyedListNode.getNodeType().getLocalName(), unkeyedListNodeSize.getNodeType()
                .getLocalName());
        assertNotNull(unkeyedListNodeCreated);
    }

    @Test
    public void immutableChoiceNodeBuilderTest() {
        final ChoiceNode choiceNode = ImmutableChoiceNodeBuilder.create(1).withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        final ChoiceNode choiceNodeCreated = ImmutableChoiceNodeBuilder.create(choiceNode).build();
        assertEquals(choiceNodeCreated.getIdentifier(), choiceNode.getIdentifier());
    }


    @Test(expected=NullPointerException.class)
    public void immutableAugmentationNodeBuilderExceptionTest() {
        ImmutableAugmentationNodeBuilder.create(1).build();
    }

    @Test(expected=NullPointerException.class)
    public void immutableContainerNodeBuilderExceptionTest() {
        final ContainerNode immutableContainerNode = ImmutableContainerNodeBuilder.create(1)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
        assertNotNull(immutableContainerNode);
        final ContainerSchemaNode containerSchemaNode = mock(ContainerSchemaNode.class);
        ImmutableContainerNodeSchemaAwareBuilder.create(containerSchemaNode, immutableContainerNode)
                .withNodeIdentifier(NODE_IDENTIFIER_LIST)
                .build();
    }

    @Test(expected=NullPointerException.class)
    public void immutableLeafSetNodeBuilderExceptionTest() {
        final LeafSetNode<?> leafSetNode = ImmutableLeafSetNodeBuilder.create(1).withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST)
                .build();
        assertNotNull(leafSetNode);
        ImmutableLeafSetNodeSchemaAwareBuilder.create(mock(LeafListSchemaNode.class), leafSetNode).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableLeafSetEntryNodeSchemaAwareBuilderExceptionTest() {
        final LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        ImmutableLeafSetEntryNodeSchemaAwareBuilder.create(leafListSchemaNode).withNodeIdentifier(BAR_PATH).build();
    }

    @Test(expected=NullPointerException.class)
    public void immutableMapEntryNodeBuilderExceptionTest() {
        ImmutableMapEntryNodeBuilder.create(1).build();
    }

    @Test(expected=NullPointerException.class)
    public void immutableYangModeledAnyXmlNodeBuilderExceptionTest() {
        ImmutableYangModeledAnyXmlNodeBuilder.create(mock(YangModeledAnyXmlSchemaNode.class), 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableUnkeyedListNodeBuilderExceptionTest() {
        ImmutableUnkeyedListNodeBuilder.create().withNodeIdentifier(NODE_IDENTIFIER_LEAF)
                .removeChild(NODE_IDENTIFIER_LIST).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapNotSchemaAwareExceptionTest1() {
        ImmutableOrderedMapNodeBuilder.create(getImmutableMapNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableMapNodeSchemaAwareExceptionTest() {
        ImmutableMapNodeSchemaAwareBuilder.create(list, getImmutableMapNode()).withNodeIdentifier(NODE_IDENTIFIER_LIST).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapSchemaAwareExceptionTest1() {
        ImmutableOrderedMapNodeSchemaAwareBuilder.create(list).withNodeIdentifier(NODE_IDENTIFIER_LIST).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapSchemaAwareExceptionTest2() {
        ImmutableOrderedMapNodeSchemaAwareBuilder.create(list, getImmutableMapNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedLeafSetNodeExceptionTest1() {
        ImmutableOrderedLeafSetNodeBuilder.create(getImmutableLeafSetNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedLeafSetNodeSchemaAwareExceptionTest1() {
        ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(leafList).withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST).build();
    }

    private static LeafSetNode<?> getImmutableLeafSetNode() {
        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafSetBuilder = Builders.leafSetBuilder();
        leafSetBuilder.withNodeIdentifier(NODE_IDENTIFIER_LEAF_LIST);
        leafSetBuilder.addChild(LEAF_SET_ENTRY_NODE);
        return leafSetBuilder.build();
    }

    private static MapNode getImmutableMapNode() {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(NODE_IDENTIFIER_LIST).withChild(LIST_MAIN_CHILD_1).build();
    }

    private static MapNode getImmutableOrderedMapNode() {
        return ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(NODE_IDENTIFIER_LIST).withChild(LIST_MAIN_CHILD_1)
                .build();
    }
}
