/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeLeafOnlyAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.ChoiceContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.ChoiceContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.choice.identifier.ExtendedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.choice.identifier.extended.ExtendedIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.top.level.list.NestedListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.controller.md.sal.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.controller.md.sal.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

public class NormalizedNodeSerializeDeserializeTest extends AbstractBindingRuntimeTest{

    public static final String TOP_LEVEL_LIST_FOO_KEY_VALUE = "foo";
    public static final TopLevelListKey TOP_LEVEL_LIST_FOO_KEY = new TopLevelListKey(TOP_LEVEL_LIST_FOO_KEY_VALUE);

    public static final QName TOP_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:yangtools:test:binding", "2014-07-01", "top");
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName TOP_LEVEL_LEAF_LIST_QNAME = QName.create(TOP_QNAME, "top-level-leaf-list");
    public static final QName TOP_LEVEL_ORDERED_LEAF_LIST_QNAME = QName.create(TOP_QNAME, "top-level-ordered-leaf-list");
    public static final QName NESTED_LIST_QNAME = QName.create(TOP_QNAME, "nested-list");
    public static final QName NESTED_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName CHOICE_CONTAINER_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:yangtools:test:binding", "2014-07-01", "choice-container");
    public static final QName CHOICE_IDENTIFIER_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "identifier");
    public static final QName CHOICE_IDENTIFIER_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "id");
    public static final QName SIMPLE_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "simple-id");
    public static final QName EXTENDED_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "extended-id");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_LEVEL_LIST_FOO_KEY).toInstance();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES =
            BA_TOP_LEVEL_LIST.augmentation(TreeComplexUsesAugment.class);

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(TOP_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TOP_LEVEL_LIST_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_FOO_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(new YangInstanceIdentifier.NodeIdentifierWithPredicates(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE));
    public static final YangInstanceIdentifier BI_CHOICE_CONTAINER_PATH = YangInstanceIdentifier.of(CHOICE_CONTAINER_QNAME);

    private BindingNormalizedNodeCodecRegistry registry;

    @Before
    public void setup() {
        super.setup();
        JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void containerToNormalized() {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top());
        ContainerNode topNormalized = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(mapNodeBuilder(TOP_LEVEL_LIST_QNAME).build()).build();
        assertEquals(topNormalized, entry.getValue());
    }

    @Test
    public void containerFromNormalized() {
        ContainerNode topNormalized = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(mapNodeBuilder(TOP_LEVEL_LIST_QNAME).build()).build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topNormalized);
        assertEquals(top(), entry.getValue());
    }

    @Test
    public void listWithKeysToNormalized() {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(BA_TOP_LEVEL_LIST, topLevelList(TOP_LEVEL_LIST_FOO_KEY));
        MapEntryNode topLevelListNormalized = ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(
                        new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .build();
        assertEquals(topLevelListNormalized, entry.getValue());
    }

    @Test
    public void listWithKeysFromNormalized() {
        MapEntryNode topLevelListNormalized = ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(
                        new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry =
                registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH, topLevelListNormalized);
        assertEquals(topLevelList(TOP_LEVEL_LIST_FOO_KEY), entry.getValue());
    }

    @Test
    public void leafOnlyAugmentationToNormalized() {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(BA_TREE_LEAF_ONLY, new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build());
        Set<QName> augmentationChildren = new HashSet<>();
        augmentationChildren.add(SIMPLE_VALUE_QNAME);
        AugmentationNode augmentationNode = ImmutableAugmentationNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren))
                .withChild(leafNode(SIMPLE_VALUE_QNAME, "simpleValue"))
                .build();
        assertEquals(augmentationNode, entry.getValue());
    }

    @Test
    public void leafOnlyAugmentationFromNormalized() {
        Set<QName> augmentationChildren = new HashSet<>();
        augmentationChildren.add(SIMPLE_VALUE_QNAME);
        AugmentationNode augmentationNode = ImmutableAugmentationNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren))
                .withChild(leafNode(SIMPLE_VALUE_QNAME, "simpleValue"))
                .build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH.node(
                        new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren)), augmentationNode);
        assertEquals(new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build(), entry.getValue());
    }

    @Test
    public void leafListToNormalized() {
        List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        Top top = new TopBuilder().setTopLevelLeafList(topLevelLeafList).build();

        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top);
        ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(ImmutableLeafSetNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_LEVEL_LEAF_LIST_QNAME))
                        .withChild(
                                ImmutableLeafSetEntryNodeBuilder.create()
                                        .withNodeIdentifier(new YangInstanceIdentifier.NodeWithValue(TOP_LEVEL_LEAF_LIST_QNAME, "foo"))
                                        .withValue("foo")
                                        .build())
                        .build())
                .build();
        assertEquals(containerNode, entry.getValue());
    }

    @Test
    public void orderedleafListToNormalized() {
        List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        Top top = new TopBuilder().setTopLevelOrderedLeafList(topLevelLeafList).build();

        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top);
        ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(ImmutableOrderedLeafSetNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME))
                        .withChild(
                                ImmutableLeafSetEntryNodeBuilder.create()
                                        .withNodeIdentifier(new YangInstanceIdentifier.NodeWithValue(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME, "foo"))
                                        .withValue("foo")
                                        .build())
                        .build())
                .build();
        assertEquals(containerNode, entry.getValue());
    }

    @Test
    public void leafListFromNormalized() {
        ContainerNode topWithLeafList = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(ImmutableLeafSetNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_LEVEL_LEAF_LIST_QNAME))
                        .withChild(ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                                new YangInstanceIdentifier.NodeWithValue(TOP_LEVEL_LEAF_LIST_QNAME, "foo")).withValue("foo").build()).build())
                .build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topWithLeafList);
        List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        Top top = new TopBuilder().setTopLevelLeafList(topLevelLeafList).build();
        assertEquals(top, entry.getValue());
    }

    @Test
    public void orderedLeafListFromNormalized() {
        ContainerNode topWithLeafList = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(ImmutableOrderedLeafSetNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier
                        (TOP_LEVEL_ORDERED_LEAF_LIST_QNAME))
                        .withChild(ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                                new YangInstanceIdentifier.NodeWithValue(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME, "foo")).withValue("foo").build()).build())
                .build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topWithLeafList);
        List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        Top top = new TopBuilder().setTopLevelOrderedLeafList(topLevelLeafList).build();
        assertEquals(top, entry.getValue());
    }

    @Test
    public void choiceToNormalized() {
        ChoiceContainer choiceContainerBA = new ChoiceContainerBuilder().setIdentifier(new ExtendedBuilder().setExtendedId(
                new ExtendedIdBuilder().setId("identifier_value").build()).build()).build();
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(ChoiceContainer.class), choiceContainerBA);
        ContainerNode choiceContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_CONTAINER_QNAME))
                .withChild(ImmutableChoiceNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                        .withChild(ImmutableContainerNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(EXTENDED_ID_QNAME))
                                .withChild(leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value")).build()).build())
                .build();
        assertEquals(choiceContainer, entry.getValue());
    }

    @Test
    public void choiceFromNormalized() {
        ContainerNode choiceContainerBI = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_CONTAINER_QNAME))
                .withChild(ImmutableChoiceNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                        .withChild(ImmutableContainerNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(EXTENDED_ID_QNAME))
                                .withChild(leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value")).build()).build())
                .build();
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_CHOICE_CONTAINER_PATH, choiceContainerBI);
        ChoiceContainer choiceContainerBA = new ChoiceContainerBuilder().setIdentifier(new ExtendedBuilder().setExtendedId(
                new ExtendedIdBuilder().setId("identifier_value").build()).build()).build();
        assertEquals(choiceContainerBA, entry.getValue());
    }

    @Test
    public void orderedListToNormalized() {
        InstanceIdentifier<TopLevelList> ii = BA_TOP_LEVEL_LIST;
        List<NestedList> nestedLists = new ArrayList<>();
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("foo")).build());
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("bar")).build());
        TopLevelList topLevelList = new TopLevelListBuilder().setKey(TOP_LEVEL_LIST_FOO_KEY).setNestedList(nestedLists).build();
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = registry.toNormalizedNode(ii, topLevelList);
        MapEntryNode foo = mapEntryBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(
                        ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(NESTED_LIST_QNAME))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar")).build()).build();
        assertEquals(foo, entry.getValue());
    }

    @Test
    public void orderedListFromNormalized() {
        MapEntryNode foo = mapEntryBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(
                        ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(NESTED_LIST_QNAME))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar")).build()).build();
        InstanceIdentifier<TopLevelList> ii = BA_TOP_LEVEL_LIST;

        Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH, foo);
        List<NestedList> nestedLists = new ArrayList<>();
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("foo")).build());
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("bar")).build());
        TopLevelList topLevelList = new TopLevelListBuilder().setKey(TOP_LEVEL_LIST_FOO_KEY).setNestedList(nestedLists).build();
        assertEquals(topLevelList, entry.getValue());
    }
}
