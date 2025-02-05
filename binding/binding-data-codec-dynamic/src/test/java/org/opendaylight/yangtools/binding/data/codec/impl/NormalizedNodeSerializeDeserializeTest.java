/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.ThirdParty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TopChoiceAugment2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1.AugmentChoice1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment1.augment.choice1.Case1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment2.augment.choice2.Case11Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.augment2.augment.choice2.case11.Case11ChoiceCaseContainerBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListKey;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.Root;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class NormalizedNodeSerializeDeserializeTest extends AbstractBindingCodecTest {
    public static final String TOP_LEVEL_LIST_FOO_KEY_VALUE = "foo";
    public static final TopLevelListKey TOP_LEVEL_LIST_FOO_KEY = new TopLevelListKey(TOP_LEVEL_LIST_FOO_KEY_VALUE);

    public static final QName TOP_QNAME = Top.QNAME;
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName TOP_LEVEL_LEAF_LIST_QNAME = QName.create(TOP_QNAME, "top-level-leaf-list");
    public static final QName TOP_LEVEL_ORDERED_LEAF_LIST_QNAME = QName.create(TOP_QNAME,
        "top-level-ordered-leaf-list");
    public static final QName NESTED_LIST_QNAME = QName.create(TOP_QNAME, "nested-list");
    public static final QName NESTED_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");
    public static final QName CHOICE_CONTAINER_QNAME = ChoiceContainer.QNAME;
    public static final QName CHOICE_IDENTIFIER_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "identifier");
    public static final QName CHOICE_IDENTIFIER_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "id");
    public static final QName EXTENDED_ID_QNAME = QName.create(CHOICE_CONTAINER_QNAME, "extended-id");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(ThirdParty.QNAME, "simple-value");

    private static final DataObjectIdentifier<TopLevelList> BA_TOP_LEVEL_LIST =
        DataObjectIdentifier.builder(Top.class).child(TopLevelList.class, TOP_LEVEL_LIST_FOO_KEY).build();

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(TOP_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TOP_LEVEL_LIST_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_FOO_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME,
                TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE));
    public static final YangInstanceIdentifier BI_CHOICE_CONTAINER_PATH = YangInstanceIdentifier.of(
        CHOICE_CONTAINER_QNAME);

    @Test
    void containerToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(DataObjectIdentifier.builder(Top.class).build(), top());
        assertEquals(getEmptyTop(), entry.node());
    }

    @Test
    void containerFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_PATH, getEmptyTop());
        assertEquals(top(), entry.getValue());
    }

    private static ContainerNode getEmptyTop() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TOP_QNAME)).build();
    }

    private static final QName AGUMENT_STRING_Q = QName.create(TOP_QNAME, "augmented-string");
    private static final String AUGMENT_STRING_VALUE = "testingEquals";
    private static final QName AUGMENT_INT_Q = QName.create(TOP_QNAME, "augmented-int");
    private static final int AUGMENT_INT_VALUE = 44;

    @Test
    void equalsWithAugment() {
        final var topNormalizedWithAugments =
            getNormalizedTopWithChildren(ImmutableNodes.leafNode(AGUMENT_STRING_Q, AUGMENT_STRING_VALUE));
        final var topNormalized = getEmptyTop();

        final var entry = codecContext.fromNormalizedNode(BI_TOP_PATH, topNormalized);
        final var entryWithAugments = codecContext.fromNormalizedNode(BI_TOP_PATH, topNormalizedWithAugments);

        // Equals on other with no augmentation should be false
        assertNotEquals(top(), entryWithAugments.getValue());
        // Equals on other(reversed) with no augmentation should be false
        assertNotEquals(entryWithAugments.getValue(), top());
        // Equals on other(lazy) with no augmentation should be false
        assertNotEquals(entry.getValue(), entryWithAugments.getValue());
        // Equals on other(lazy, reversed) with no augmentation should be false
        assertNotEquals(entryWithAugments.getValue(), entry.getValue());

        final Top topWithAugments = topWithAugments(
            Map.of(Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build()));
        // Equals other with same augment should be true
        assertEquals(topWithAugments, entryWithAugments.getValue());
        // Equals other with same augment should be true
        assertEquals(entryWithAugments.getValue(), topWithAugments);
        // Equals on self should be true
        assertEquals(entryWithAugments.getValue(), entryWithAugments.getValue());

        final Top topWithAugmentsDiffValue = topWithAugments(
            Map.of(Top1.class, new Top1Builder().setAugmentedString("differentValue").build()));
        assertNotEquals(topWithAugmentsDiffValue, entryWithAugments.getValue());
        assertNotEquals(entryWithAugments.getValue(), topWithAugmentsDiffValue);
    }

    @Test
    void equalsWithMultipleAugments() {
        final var topNormalizedWithAugments = getNormalizedTopWithChildren(
            ImmutableNodes.leafNode(AGUMENT_STRING_Q, AUGMENT_STRING_VALUE),
            ImmutableNodes.leafNode(AUGMENT_INT_Q, AUGMENT_INT_VALUE));
        final var entryWithAugments = codecContext.fromNormalizedNode(BI_TOP_PATH, topNormalizedWithAugments);
        var topWithAugments = topWithAugments(Map.of(
            Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build(),
            Top2.class, new Top2Builder().setAugmentedInt(AUGMENT_INT_VALUE).build()));

        assertEquals(topWithAugments, entryWithAugments.getValue());
        assertEquals(entryWithAugments.getValue(), topWithAugments);

        topWithAugments = topWithAugments(Map.of(
            Top1.class, new Top1Builder().setAugmentedString(AUGMENT_STRING_VALUE).build(),
            Top2.class, new Top2Builder().setAugmentedInt(999).build()));

        assertNotEquals(topWithAugments, entryWithAugments.getValue());
        assertNotEquals(entryWithAugments.getValue(), topWithAugments);
    }

    private static ContainerNode getNormalizedTopWithChildren(final DataContainerChild... children) {
        final var builder = ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TOP_QNAME));
        for (var child : children) {
            builder.withChild(child);
        }
        return builder
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TOP_LEVEL_LIST_QNAME))
                .build())
            .build();
    }

    private static Top topWithAugments(
            final Map<Class<? extends Augmentation<Top>>, ? extends Augmentation<Top>> augments) {
        final TopBuilder topBuilder = new TopBuilder();
        for (Augmentation<Top> augment : augments.values()) {
            topBuilder.addAugmentation(augment);
        }
        return topBuilder.build();
    }

    @Test
    void listWithKeysToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, topLevelList(TOP_LEVEL_LIST_FOO_KEY));
        assertEquals(ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME,
                TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .build(),
            entry.node());
    }

    @Test
    void listWithKeysFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH,
            ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME,
                    TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .build());
        assertEquals(topLevelList(TOP_LEVEL_LIST_FOO_KEY), entry.getValue());
    }

    @Test
    void leafOnlyAugmentationToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST,
            topLevelList(TOP_LEVEL_LIST_FOO_KEY,
                new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build()));
        assertEquals(ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME,
                TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(ImmutableNodes.leafNode(SIMPLE_VALUE_QNAME, "simpleValue")) // augmentation child
            .build(),
            entry.node());
    }

    @Test
    void leafOnlyAugmentationFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH,
            ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME,
                    TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(ImmutableNodes.leafNode(SIMPLE_VALUE_QNAME, "simpleValue")) // augmentation child
                .build());
        assertEquals(
            topLevelList(TOP_LEVEL_LIST_FOO_KEY,
                new TreeLeafOnlyAugmentBuilder().setSimpleValue("simpleValue").build()),
            entry.getValue());
    }

    @Test
    void orderedleafListToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(DataObjectIdentifier.builder(Top.class).build(),
            new TopBuilder().setTopLevelOrderedLeafList(List.of("foo")).build());
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.newUserLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME, "foo"))
                .build())
            .build(),
            entry.node());
    }

    @Test
    void leafListToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(DataObjectIdentifier.builder(Top.class).build(),
            new TopBuilder().setTopLevelLeafList(Set.of("foo")).build());
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TOP_LEVEL_LEAF_LIST_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(TOP_LEVEL_LEAF_LIST_QNAME, "foo"))
                .build())
            .build(),
            entry.node());
    }

    @Test
    void leafListFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TOP_LEVEL_LEAF_LIST_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(TOP_LEVEL_LEAF_LIST_QNAME, "foo"))
                .build())
            .build());
        assertEquals(new TopBuilder().setTopLevelLeafList(Set.of("foo")).build(), entry.getValue());
    }

    @Test
    void orderedLeafListFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.newUserLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(TOP_LEVEL_ORDERED_LEAF_LIST_QNAME, "foo"))
                .build())
            .build());
        assertEquals(new TopBuilder().setTopLevelOrderedLeafList(List.of("foo")).build(), entry.getValue());
    }

    @Test
    void choiceToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(InstanceIdentifier.create(ChoiceContainer.class),
            new ChoiceContainerBuilder()
                .setIdentifier(new ExtendedBuilder()
                    .setExtendedId(new ExtendedIdBuilder().setId("identifier_value").build())
                    .build())
                .build());
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CHOICE_CONTAINER_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(EXTENDED_ID_QNAME))
                    .withChild(ImmutableNodes.leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value"))
                    .build())
                .build())
            .build(),
            entry.node());
    }

    @Test
    void test4798() {
        final var containerIdentifierQname4798 = Root.QNAME;
        final var choiceIdentifierQname4798 = QName.create(containerIdentifierQname4798, "bug4798-choice");
        final var nestedListQname4798 = QName.create(containerIdentifierQname4798, "list-in-case");
        final var nestedListKeyQname4798 = QName.create(containerIdentifierQname4798, "test-leaf");
        final var nestedContainerValidQname = QName.create(containerIdentifierQname4798, "case-b-container");
        final var nestedContainerOuterQname = QName.create(containerIdentifierQname4798, "outer-container");
        final var nestedContainerLeafOuterQname = QName.create(containerIdentifierQname4798, "leaf-in-outer-container");

        final var yangInstanceIdentifierOuter = YangInstanceIdentifier.of(
            containerIdentifierQname4798);
        final var containerNodeOuter = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(containerIdentifierQname4798))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(nestedContainerOuterQname))
                .withChild(ImmutableNodes.leafNode(nestedContainerLeafOuterQname, "bar"))
                .build())
            .build();
        final var entryContainer = codecContext.fromNormalizedNode(yangInstanceIdentifierOuter, containerNodeOuter);
        assertNotNull(entryContainer.getValue());
        assertNotNull(entryContainer.getKey());

        final var nodeIdentifierWithPredicates4798 =
                NodeIdentifierWithPredicates.of(nestedListQname4798, nestedListKeyQname4798, "foo");
        final var yangInstanceIdentifier4798 = YangInstanceIdentifier.of(
            containerIdentifierQname4798)
                .node(choiceIdentifierQname4798)
                .node(nestedListQname4798)
                .node(nodeIdentifierWithPredicates4798);

        final var yangInstanceIdentifierValid = YangInstanceIdentifier.of(
            containerIdentifierQname4798)
                .node(choiceIdentifierQname4798)
                .node(nestedContainerValidQname)
                .node(nestedListQname4798)
                .node(nodeIdentifierWithPredicates4798);
        final var containerNodeValid = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(containerIdentifierQname4798))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(choiceIdentifierQname4798))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(nestedContainerValidQname))
                    .withChild(ImmutableNodes.newSystemMapBuilder()
                        .withNodeIdentifier(new NodeIdentifier(nestedListQname4798))
                        .withChild(ImmutableNodes.newMapEntryBuilder()
                            .withNodeIdentifier(
                                NodeIdentifierWithPredicates.of(nestedListQname4798, nestedListKeyQname4798, "foo"))
                            .withChild(ImmutableNodes.leafNode(nestedListKeyQname4798, "foo"))
                            .build())
                        .withChild(ImmutableNodes.newMapEntryBuilder()
                            .withNodeIdentifier(
                                NodeIdentifierWithPredicates.of(nestedListQname4798, nestedListKeyQname4798, "bar"))
                            .withChild(ImmutableNodes.leafNode(nestedListKeyQname4798, "bar"))
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        var msg = assertThrows(IllegalArgumentException.class,
            () -> codecContext.fromNormalizedNode(yangInstanceIdentifierValid, containerNodeValid))
            .getMessage();
        assertEquals("Expecting either a MapEntryNode or an UnkeyedListEntryNode, not ContainerNode", msg);

        final var containerNode4798 = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(containerIdentifierQname4798))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(choiceIdentifierQname4798))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(nestedListQname4798))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(nestedListQname4798, nestedListKeyQname4798, "foo"))
                        .withChild(ImmutableNodes.leafNode(nestedListKeyQname4798, "foo"))
                        .build())
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(nestedListQname4798, nestedListKeyQname4798, "bar"))
                        .withChild(ImmutableNodes.leafNode(nestedListKeyQname4798, "bar"))
                        .build())
                    .build())
                .build())
            .build();

        msg = assertThrows(IllegalArgumentException.class,
            () -> codecContext.fromNormalizedNode(yangInstanceIdentifier4798, containerNode4798))
            .getMessage();
        assertEquals("Expecting either a MapEntryNode or an UnkeyedListEntryNode, not ContainerNode", msg);
    }

    @Test
    void choiceFromNormalized() {
        final var choiceContainerBI = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CHOICE_CONTAINER_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(CHOICE_IDENTIFIER_QNAME))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(EXTENDED_ID_QNAME))
                    .withChild(ImmutableNodes.leafNode(CHOICE_IDENTIFIER_ID_QNAME, "identifier_value"))
                    .build())
                .build())
            .build();
        final var entry = codecContext.fromNormalizedNode(BI_CHOICE_CONTAINER_PATH,choiceContainerBI);
        final var choiceContainerBA = new ChoiceContainerBuilder().setIdentifier(new ExtendedBuilder()
            .setExtendedId(new ExtendedIdBuilder().setId("identifier_value").build()).build()).build();
        assertEquals(choiceContainerBA, entry.getValue());
    }

    @Test
    void orderedLisToNormalized() {
        final var entry = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, new TopLevelListBuilder()
            .withKey(TOP_LEVEL_LIST_FOO_KEY)
            .setNestedList(List.of(
                new NestedListBuilder().withKey(new NestedListKey("foo")).build(),
                new NestedListBuilder().withKey(new NestedListKey("bar")).build()))
            .build());
        assertEquals(ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TOP_LEVEL_LIST_QNAME,
                TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(ImmutableNodes.newUserMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(NESTED_LIST_QNAME))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                    .withChild(ImmutableNodes.leafNode(NESTED_LIST_KEY_QNAME, "foo"))
                    .build())
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar"))
                    .withChild(ImmutableNodes.leafNode(NESTED_LIST_KEY_QNAME, "bar"))
                    .build())
                .build())
            .build(),
            entry.node());
    }

    @Test
    void orderedLisFromNormalized() {
        final var entry = codecContext.fromNormalizedNode(BI_TOP_LEVEL_LIST_FOO_PATH,
            ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                    TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
                .withChild(ImmutableNodes.newUserMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(NESTED_LIST_QNAME))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "foo"))
                        .withChild(ImmutableNodes.leafNode(NESTED_LIST_KEY_QNAME, "foo"))
                        .build())
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(NESTED_LIST_QNAME, NESTED_LIST_KEY_QNAME, "bar"))
                        .withChild(ImmutableNodes.leafNode(NESTED_LIST_KEY_QNAME, "bar"))
                        .build())
                    .build())
                .build());
        assertEquals(new TopLevelListBuilder()
            .withKey(TOP_LEVEL_LIST_FOO_KEY)
            .setNestedList(List.of(
                new NestedListBuilder().withKey(new NestedListKey("foo")).build(),
                new NestedListBuilder().withKey(new NestedListKey("bar")).build()))
            .build(),
            entry.getValue());
    }

    @Test
    void augmentMultipleChoices() {
        final var augmentChoice1QName = AugmentChoice1.QNAME;
        final var augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final var containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final var leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final var augmentChoice1Id = new NodeIdentifier(augmentChoice1QName);
        final var augmentChoice2Id = new NodeIdentifier(augmentChoice2QName);
        final var containerId = new NodeIdentifier(containerQName);

        final var top = new TopBuilder().addAugmentation(
            // top is augmented with choice1 having case1
            new TopChoiceAugment1Builder().setAugmentChoice1(
                new Case1Builder().addAugmentation(
                    // case1 is augmented with choice2 having case11 (with container having leaf)
                    new TopChoiceAugment2Builder().setAugmentChoice2(
                        new Case11Builder().setCase11ChoiceCaseContainer(
                                new Case11ChoiceCaseContainerBuilder()
                                        .setCase11ChoiceCaseLeaf("leaf-value").build()
                        ).build()
                    ).build()
                ).build()
            ).build()
        ).build();

        final var biResult = codecContext.toNormalizedDataObject(InstanceIdentifier.create(Top.class), top);

        final var topNormalized = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(augmentChoice1Id)
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(augmentChoice2Id)
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(containerId)
                        .withChild(ImmutableNodes.leafNode(leafQName, "leaf-value"))
                        .build())
                    .build())
                .build())
            .build();

        assertEquals(BI_TOP_PATH, biResult.path());
        assertEquals(topNormalized, biResult.node());

        final var baResult = codecContext.fromNormalizedNode(BI_TOP_PATH, topNormalized);

        assertEquals(InstanceIdentifier.create(Top.class), baResult.getKey());
        assertEquals(top, baResult.getValue());
    }
}
