/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;

public class BuilderTest {
    private static final QName ROOT_CONTAINER = QName.create("test.namespace.builder.test", "2016-01-01", "root-container");
    private static final QName LIST_MAIN = QName.create(ROOT_CONTAINER, "list-ordered-by-user-with-key");
    private static final QName LEAF_LIST_MAIN = QName.create(ROOT_CONTAINER, "leaf-list-ordered-by-user");
    private static final QName LIST_MAIN_CHILD_QNAME_1 = QName.create(ROOT_CONTAINER, "leaf-a");
    private static final YangInstanceIdentifier.NodeIdentifier LIST_MAIN_NI = new YangInstanceIdentifier.NodeIdentifier
            (LIST_MAIN);
    private static final YangInstanceIdentifier.NodeIdentifier LEAF_LIST_MAIN_NI = new YangInstanceIdentifier
            .NodeIdentifier(LEAF_LIST_MAIN);
    private static final MapEntryNode LIST_MAIN_CHILD_1 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1);
    private static final MapEntryNode LIST_MAIN_CHILD_2 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 2);
    private static final MapEntryNode LIST_MAIN_CHILD_3 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 3);
    private static final Integer SIZE = 3;
    private static final YangInstanceIdentifier.NodeWithValue BAR_PATH = new YangInstanceIdentifier
            .NodeWithValue(LEAF_LIST_MAIN, "bar");
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
        final SchemaContext schema = RetestUtils.parseYangSources(leafRefTestYang);
        final Module module = schema.getModules().iterator().next();
        final DataSchemaNode root = module.getDataChildByName(ROOT_CONTAINER);
        list = (ListSchemaNode)((ContainerEffectiveStatementImpl) root).getDataChildByName(LIST_MAIN);
        leafList = (LeafListSchemaNode)((ContainerEffectiveStatementImpl) root).getDataChildByName(LEAF_LIST_MAIN);
    }

    @Test
    public void immutableOrderedMapBuilderTest() {
        final LinkedList<MapEntryNode> mapEntryNodeColl = new LinkedList();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);
        final Map<QName, Object> keys = new HashMap<QName, Object>();
        keys.put(LIST_MAIN_CHILD_QNAME_1, 1);
        final YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(LIST_MAIN, keys);
        final OrderedMapNode orderedMapNodeCreateNull = ImmutableOrderedMapNodeBuilder.create()
                .withNodeIdentifier(LIST_MAIN_NI)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(mapEntryNodeColl)
                .build();
        final OrderedMapNode orderedMapNodeCreateSize = ImmutableOrderedMapNodeBuilder.create(SIZE)
                .withNodeIdentifier(LIST_MAIN_NI)
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

        assertEquals(SIZE, (Integer) orderedMapNodeCreateNull.getSize());
        assertEquals(orderedMapNodeCreateNode.getSize(), orderedMapNodeCreateNull.getSize() - 1);
        assertEquals(LIST_MAIN_NI, orderedMapNodeCreateSize.getIdentifier());
        assertEquals(orderedMapNodeCreateNull.getChild(0), LIST_MAIN_CHILD_1);
        assertEquals((Integer) orderedMapNodeCreateNull.getValue().size(), SIZE);
        assertNotNull(orderedMapNodeCreateNull.hashCode());
        assertEquals((Integer) orderedMapNodeCreateNull.getValue().size(), SIZE);
        assertEquals(orderedMapNodeSchemaAware.getChild(0), orderedMapNodeSchemaAwareMapNodeConst.getChild(0));
    }

    @Test
    public void immutableOrderedLeafSetNodeBuilderTest() {
        final NormalizedNode orderedLeafSet = ImmutableOrderedLeafSetNodeBuilder.create()
                .withNodeIdentifier(LEAF_LIST_MAIN_NI)
                .withChild(LEAF_SET_ENTRY_NODE)
                .withChildValue("baz")
                .removeChild(BAR_PATH)
                .build();
        final UnmodifiableCollection leafSetCollection = (UnmodifiableCollection)orderedLeafSet.getValue();
        final NormalizedNode orderedMapNodeSchemaAware = ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(leafList)
                .withChildValue("baz")
                .build();
        final UnmodifiableCollection SchemaAwareleafSetCollection = (UnmodifiableCollection)orderedMapNodeSchemaAware
                .getValue();
        assertEquals(1, leafSetCollection.size());
        assertEquals(1, SchemaAwareleafSetCollection.size());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapNotSchemaAwareException1() {
        ImmutableOrderedMapNodeBuilder.create(getImmutableMapNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapSchemaAwareException1() {
        ImmutableOrderedMapNodeSchemaAwareBuilder.create(list).withNodeIdentifier(LIST_MAIN_NI).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedMapSchemaAwareException2() {
        ImmutableOrderedMapNodeSchemaAwareBuilder.create(list, getImmutableMapNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedLeafSetNodeException1() {
        ImmutableOrderedLeafSetNodeBuilder.create(getImmutableLeafSetNode()).build();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void immutableOrderedLeafSetNodeSchemaAwareException1() {
        ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(leafList).withNodeIdentifier(LEAF_LIST_MAIN_NI).build();
    }

    private static LeafSetNode getImmutableLeafSetNode() {
        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafSetBuilder = Builders.leafSetBuilder();
        leafSetBuilder.withNodeIdentifier(LEAF_LIST_MAIN_NI);
        leafSetBuilder.addChild(LEAF_SET_ENTRY_NODE);
        return leafSetBuilder.build();
    }

    private static MapNode getImmutableMapNode() {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(LIST_MAIN_NI).withChild(LIST_MAIN_CHILD_1).build();
    }

    private static MapNode getImmutableOrderedMapNode() {
        return ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(LIST_MAIN_NI).withChild(LIST_MAIN_CHILD_1)
                .build();
    }
}