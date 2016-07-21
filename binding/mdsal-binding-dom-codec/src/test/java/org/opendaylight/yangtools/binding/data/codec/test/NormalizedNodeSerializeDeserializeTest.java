/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.augmentationBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.AugmentChoice1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.augment.choice1.Case1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.augment.choice1.case1.augment.choice2.Case11Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.augment.choice1.case1.augment.choice2.case11.Case11ChoiceCaseContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.ChoiceContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.ChoiceContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice.identifier.ExtendedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.choice.identifier.extended.ExtendedIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListKey;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.Root;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;

public class NormalizedNodeSerializeDeserializeTest extends AbstractBindingRuntimeTest{

    public static final String TOP_LEVEL_LIST_FOO_KEY_VALUE = "foo";
    public static final TopLevelListKey TOP_LEVEL_LIST_FOO_KEY = new TopLevelListKey(TOP_LEVEL_LIST_FOO_KEY_VALUE);

    public static final QName TOP_QNAME = Top.QNAME;
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName TOP_LEVEL_LEAF_LIST_QNAME = QName.create(TOP_QNAME, "top-level-leaf-list");
    public static final QName TOP_LEVEL_ORDERED_LEAF_LIST_QNAME = QName.create(TOP_QNAME, "top-level-ordered-leaf-list");
    public static final QName NESTED_LIST_QNAME = QName.create(TOP_QNAME, "nested-list");
    public static final QName NESTED_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName CHOICE_CONTAINER_QNAME = ChoiceContainer.QNAME;
    public static final QName CHOICE_IDENTIFIER_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "identifier");
    public static final QName CHOICE_IDENTIFIER_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "id");
    public static final QName SIMPLE_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "simple-id");
    public static final QName EXTENDED_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "extended-id");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_LEVEL_LIST_FOO_KEY).build();
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

    @Override
    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void containerToNormalized() {
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top());
        final ContainerNode topNormalized = getEmptyTop();
        assertEquals(topNormalized, entry.getValue());
    }

    @Test
    public void containerFromNormalized() {
        final ContainerNode topNormalized = getEmptyTop();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topNormalized);
        assertEquals(top(), entry.getValue());
    }

    private ContainerNode getEmptyTop() {
        return ImmutableContainerNodeBuilder.create()
                    .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                    .withChild(mapNodeBuilder(TOP_LEVEL_LIST_QNAME).build()).build();
    }

    private static final QName AGUMENT_STRING_Q = QName.create(TOP_QNAME, "augmented-string");
    private static final String AUGMENT_STRING_VALUE = "testingEquals";
    private static final QName AUGMENT_INT_Q = QName.create(TOP_QNAME, "augmented-int");
    private static final int AUGMENT_INT_VALUE = 44;

    @Test
    public void equalsWithAugment() {
        final ContainerNode topNormalizedWithAugments = getNormalizedTopWithAugments(
            augmentationBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(singleton(AGUMENT_STRING_Q)))
                .withChild(ImmutableNodes.leafNode(AGUMENT_STRING_Q, AUGMENT_STRING_VALUE))
                .build());
        final ContainerNode topNormalized = getEmptyTop();

        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topNormalized);
        final Map.Entry<InstanceIdentifier<?>, DataObject> entryWithAugments = registry.fromNormalizedNode(BI_TOP_PATH, topNormalizedWithAugments);

        // Equals on other with no augmentation should be false
        assertNotEquals(top(), entryWithAugments.getValue());
        // Equals on other(reversed) with no augmentation should be false
        assertNotEquals(entryWithAugments.getValue(), top());
        // Equals on other(lazy) with no augmentation should be false
        assertNotEquals(entry.getValue(), entryWithAugments.getValue());
        // Equals on other(lazy, reversed) with no augmentation should be false
        assertNotEquals(entryWithAugments.getValue(), entry.getValue());

        final Top topWithAugments = topWithAugments(Collections.<Class<? extends Augmentation<Top>>, Augmentation<Top>>
            singletonMap(Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build()));
        // Equals other with same augment should be true
        assertEquals(topWithAugments, entryWithAugments.getValue());
        // Equals other with same augment should be true
        assertEquals(entryWithAugments.getValue(), topWithAugments);
        // Equals on self should be true
        assertEquals(entryWithAugments.getValue(), entryWithAugments.getValue());

        final Top topWithAugmentsDiffValue = topWithAugments(Collections.<Class<? extends Augmentation<Top>>, Augmentation<Top>>
            singletonMap(Top1.class, new Top1Builder().setAugmentedString("differentValue").build()));
        assertNotEquals(topWithAugmentsDiffValue, entryWithAugments.getValue());
        assertNotEquals(entryWithAugments.getValue(), topWithAugmentsDiffValue);
    }

    @Test
    public void equalsWithMultipleAugments() {
        final ContainerNode topNormalizedWithAugments = getNormalizedTopWithAugments(
            augmentationBuilder()
                .withNodeIdentifier(
                    new YangInstanceIdentifier.AugmentationIdentifier(singleton(AGUMENT_STRING_Q)))
                .withChild(ImmutableNodes.leafNode(AGUMENT_STRING_Q, AUGMENT_STRING_VALUE))
                .build(),
            augmentationBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(singleton(AUGMENT_INT_Q)))
                .withChild(ImmutableNodes.leafNode(AUGMENT_INT_Q, AUGMENT_INT_VALUE))
                .build());

        final Map.Entry<InstanceIdentifier<?>, DataObject> entryWithAugments = registry.fromNormalizedNode(BI_TOP_PATH, topNormalizedWithAugments);
        Map<Class<? extends Augmentation<Top>>, Augmentation<Top>> augments = Maps.newHashMap();
        augments.put(Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build());
        augments.put(Top2.class, new Top2Builder().setAugmentedInt(AUGMENT_INT_VALUE).build());
        Top topWithAugments = topWithAugments(augments);

        assertEquals(topWithAugments, entryWithAugments.getValue());
        assertEquals(entryWithAugments.getValue(), topWithAugments);

        augments = Maps.newHashMap();
        augments.put(Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build());
        augments.put(Top2.class, new Top2Builder().setAugmentedInt(999).build());
        topWithAugments = topWithAugments(augments);

        assertNotEquals(topWithAugments, entryWithAugments.getValue());
        assertNotEquals(entryWithAugments.getValue(), topWithAugments);
    }

    private ContainerNode getNormalizedTopWithAugments(final AugmentationNode... augChild) {
        final DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, ContainerNode>
            builder = ImmutableContainerNodeBuilder.create();

        for (AugmentationNode augmentationNode : augChild) {
            builder.withChild(augmentationNode);
        }
        return builder.withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                    .withChild(mapNodeBuilder(TOP_LEVEL_LIST_QNAME).build()).build();
    }

    private static Top topWithAugments(final Map<Class<? extends Augmentation<Top>>, ? extends Augmentation<Top>> augments) {
        final TopBuilder topBuilder = new TopBuilder();
        for (Map.Entry<Class<? extends Augmentation<Top>>, ? extends Augmentation<Top>> augment : augments.entrySet()) {
            topBuilder.addAugmentation(augment.getKey(), augment.getValue());
        }
        return topBuilder.setTopLevelList(Collections.<TopLevelList>emptyList()).build();
    }

    @Test
    public void listWithKeysToNormalized() {
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(BA_TOP_LEVEL_LIST, topLevelList(TOP_LEVEL_LIST_FOO_KEY));
        final MapEntryNode topLevelListNormalized = ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(
                        new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .build();
        assertEquals(topLevelListNormalized, entry.getValue());
    }

    @Test
    public void listWithKeysFromNormalized() {
        final MapEntryNode topLevelListNormalized = ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(
                        new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry =
                registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH, topLevelListNormalized);
        assertEquals(topLevelList(TOP_LEVEL_LIST_FOO_KEY), entry.getValue());
    }

    @Test
    public void leafOnlyAugmentationToNormalized() {
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(BA_TREE_LEAF_ONLY, new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build());
        final Set<QName> augmentationChildren = new HashSet<>();
        augmentationChildren.add(SIMPLE_VALUE_QNAME);
        final AugmentationNode augmentationNode = ImmutableAugmentationNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren))
                .withChild(leafNode(SIMPLE_VALUE_QNAME, "simpleValue"))
                .build();
        assertEquals(augmentationNode, entry.getValue());
    }

    @Test
    public void leafOnlyAugmentationFromNormalized() {
        final Set<QName> augmentationChildren = new HashSet<>();
        augmentationChildren.add(SIMPLE_VALUE_QNAME);
        final AugmentationNode augmentationNode = ImmutableAugmentationNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren))
                .withChild(leafNode(SIMPLE_VALUE_QNAME, "simpleValue"))
                .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH.node(
                new YangInstanceIdentifier.AugmentationIdentifier(augmentationChildren)), augmentationNode);
        assertEquals(new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build(), entry.getValue());
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
    public void leafListToNormalized() {
        final List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        final Top top = new TopBuilder().setTopLevelLeafList(topLevelLeafList).build();

        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top);
        final ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
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
    public void leafListFromNormalized() {
        final ContainerNode topWithLeafList = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(ImmutableLeafSetNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_LEVEL_LEAF_LIST_QNAME))
                        .withChild(ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                                new YangInstanceIdentifier.NodeWithValue(TOP_LEVEL_LEAF_LIST_QNAME, "foo")).withValue("foo").build()).build())
                .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_PATH, topWithLeafList);
        final List<String> topLevelLeafList = new ArrayList<>();
        topLevelLeafList.add("foo");
        final Top top = new TopBuilder().setTopLevelLeafList(topLevelLeafList).build();
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
        final ChoiceContainer choiceContainerBA = new ChoiceContainerBuilder().setIdentifier(new ExtendedBuilder().setExtendedId(
                new ExtendedIdBuilder().setId("identifier_value").build()).build()).build();
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                registry.toNormalizedNode(InstanceIdentifier.create(ChoiceContainer.class), choiceContainerBA);
        final ContainerNode choiceContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_CONTAINER_QNAME))
                .withChild(ImmutableChoiceNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                        .withChild(ImmutableContainerNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(EXTENDED_ID_QNAME))
                                .withChild(leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value")).build()).build())
                .build();
        assertEquals(choiceContainer, entry.getValue());
    }

    @Test
    public void test4798() {
        final QName containerIdentifierQname4798 = Root.QNAME;
        final QName choiceIdentifierQname4798 = QName.create(containerIdentifierQname4798, "bug4798-choice");
        final QName nestedListQname4798 = QName.create(containerIdentifierQname4798, "list-in-case");
        final QName nestedListKeyQname4798 = QName.create(containerIdentifierQname4798, "test-leaf");
        final QName nestedContainerValidQname = QName.create(containerIdentifierQname4798, "case-b-container");
        final QName nestedContainerOuterQname = QName.create(containerIdentifierQname4798, "outer-container");
        final QName nestedContainerLeafOuterQname = QName.create(containerIdentifierQname4798,
                "leaf-in-outer-container");

        final YangInstanceIdentifier yangInstanceIdentifierOuter = YangInstanceIdentifier.of(containerIdentifierQname4798);
        final ContainerNode containerNodeOuter = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(containerIdentifierQname4798))
                .withChild(ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(nestedContainerOuterQname))
                        .withChild((leafNode(nestedContainerLeafOuterQname, "bar")))
                        .build())
                .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entryContainer = registry.fromNormalizedNode
                 (yangInstanceIdentifierOuter, containerNodeOuter);
        assertNotNull(entryContainer.getValue());
        assertNotNull(entryContainer.getKey());

        final YangInstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifierWithPredicates4798 = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates( nestedListQname4798, nestedListKeyQname4798, "foo" );
        final YangInstanceIdentifier yangInstanceIdentifier4798 = YangInstanceIdentifier.of(containerIdentifierQname4798)
                .node(choiceIdentifierQname4798)
                .node(nestedListQname4798)
                .node(nodeIdentifierWithPredicates4798);

        final YangInstanceIdentifier yangInstanceIdentifierValid = YangInstanceIdentifier.of(containerIdentifierQname4798)
                .node(choiceIdentifierQname4798)
                .node(nestedContainerValidQname)
                .node(nestedListQname4798)
                .node(nodeIdentifierWithPredicates4798);
        final ContainerNode containerNodeValid = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(containerIdentifierQname4798))
                .withChild(ImmutableChoiceNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(choiceIdentifierQname4798))
                        .withChild(ImmutableContainerNodeBuilder.create()
                                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(nestedContainerValidQname))
                                .withChild(ImmutableMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier
                                        .NodeIdentifier(nestedListQname4798))
                                        .withChild(mapEntry(nestedListQname4798, nestedListKeyQname4798, "foo"))
                                        .withChild(mapEntry(nestedListQname4798, nestedListKeyQname4798, "bar"))
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            registry.fromNormalizedNode
                    (yangInstanceIdentifierValid, containerNodeValid);
            fail("Incorect YangInstanceIdentifier should fail");
        } catch (IllegalStateException e) {
        }

        final ContainerNode containerNode4798 = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(containerIdentifierQname4798))
                .withChild(ImmutableChoiceNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(choiceIdentifierQname4798))
                        .withChild(ImmutableMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier
                                .NodeIdentifier(nestedListQname4798))
                                .withChild(mapEntry(nestedListQname4798, nestedListKeyQname4798, "foo"))
                                .withChild(mapEntry(nestedListQname4798, nestedListKeyQname4798, "bar"))
                                .build())
                        .build())
                .build();
        try {
            registry.fromNormalizedNode
                    (yangInstanceIdentifier4798, containerNode4798);
            fail("Incorect YangInstanceIdentifier should fail");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void choiceFromNormalized() {
        final ContainerNode choiceContainerBI = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_CONTAINER_QNAME))
                .withChild(ImmutableChoiceNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                        .withChild(ImmutableContainerNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(EXTENDED_ID_QNAME))
                                .withChild(leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value")).build()).build())
                .build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_CHOICE_CONTAINER_PATH, choiceContainerBI);
        final ChoiceContainer choiceContainerBA = new ChoiceContainerBuilder().setIdentifier(new ExtendedBuilder().setExtendedId(
                new ExtendedIdBuilder().setId("identifier_value").build()).build()).build();
        assertEquals(choiceContainerBA, entry.getValue());
    }

    @Test
    public void orderedLisToNormalized() {
        final InstanceIdentifier<TopLevelList> ii = BA_TOP_LEVEL_LIST;
        final List<NestedList> nestedLists = new ArrayList<>();
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("foo")).build());
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("bar")).build());
        final TopLevelList topLevelList = new TopLevelListBuilder().setKey(TOP_LEVEL_LIST_FOO_KEY).setNestedList(nestedLists).build();
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = registry.toNormalizedNode(ii, topLevelList);
        final MapEntryNode foo = mapEntryBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(
                        ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(NESTED_LIST_QNAME))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar")).build()).build();
        assertEquals(foo, entry.getValue());
    }

    @Test
    public void orderedLisFromNormalized() {
        final MapEntryNode foo = mapEntryBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(
                        ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(NESTED_LIST_QNAME))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                                .withChild(mapEntry(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar")).build()).build();
        final Map.Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH, foo);
        final List<NestedList> nestedLists = new ArrayList<>();
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("foo")).build());
        nestedLists.add(new NestedListBuilder().setKey(new NestedListKey("bar")).build());
        final TopLevelList topLevelList = new TopLevelListBuilder().setKey(TOP_LEVEL_LIST_FOO_KEY).setNestedList(nestedLists).build();
        assertEquals(topLevelList, entry.getValue());
    }

    @Test
    public void augmentMultipleChoices() {
        final QName augmentChoice1QName = AugmentChoice1.QNAME;
        final QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final YangInstanceIdentifier.AugmentationIdentifier aug1Id =
                new YangInstanceIdentifier.AugmentationIdentifier(Sets.newHashSet(augmentChoice1QName));
        final YangInstanceIdentifier.AugmentationIdentifier aug2Id =
                new YangInstanceIdentifier.AugmentationIdentifier(Sets.newHashSet(augmentChoice2QName));
        final YangInstanceIdentifier.NodeIdentifier augmentChoice1Id =
                new YangInstanceIdentifier.NodeIdentifier(augmentChoice1QName);
        final YangInstanceIdentifier.NodeIdentifier augmentChoice2Id =
                new YangInstanceIdentifier.NodeIdentifier(augmentChoice2QName);
        final YangInstanceIdentifier.NodeIdentifier containerId =
                new YangInstanceIdentifier.NodeIdentifier(containerQName);

        final TopBuilder tBuilder = new TopBuilder();
        final TopChoiceAugment1Builder tca1Builder = new TopChoiceAugment1Builder();
        final Case1Builder c1Builder = new Case1Builder();
        final TopChoiceAugment2Builder tca2Builder = new TopChoiceAugment2Builder();
        final Case11Builder c11Builder = new Case11Builder();
        final Case11ChoiceCaseContainerBuilder cccc1Builder = new Case11ChoiceCaseContainerBuilder();
        cccc1Builder.setCase11ChoiceCaseLeaf("leaf-value");
        c11Builder.setCase11ChoiceCaseContainer(cccc1Builder.build());
        tca2Builder.setAugmentChoice2(c11Builder.build());
        c1Builder.addAugmentation(TopChoiceAugment2.class, tca2Builder.build());
        tca1Builder.setAugmentChoice1(c1Builder.build());
        tBuilder.addAugmentation(TopChoiceAugment1.class, tca1Builder.build());
        final Top top = tBuilder.build();

        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> biResult =
                registry.toNormalizedNode(InstanceIdentifier.create(Top.class), top);

        final NormalizedNode<?, ?> topNormalized =
                containerBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TOP_QNAME))
                .withChild(augmentationBuilder().withNodeIdentifier(aug1Id)
                        .withChild(choiceBuilder().withNodeIdentifier(augmentChoice1Id)
                                .withChild(augmentationBuilder().withNodeIdentifier(aug2Id)
                                        .withChild(choiceBuilder().withNodeIdentifier(augmentChoice2Id)
                                                .withChild(containerBuilder().withNodeIdentifier(containerId)
                                                        .withChild(leafNode(leafQName, "leaf-value"))
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build()).build();

        assertEquals(BI_TOP_PATH, biResult.getKey());
        assertEquals(topNormalized, biResult.getValue());

        final Map.Entry<InstanceIdentifier<?>, DataObject> baResult = registry.fromNormalizedNode(BI_TOP_PATH, topNormalized);

        assertEquals(InstanceIdentifier.create(Top.class), baResult.getKey());
        assertEquals(top, baResult.getValue());
    }
}
