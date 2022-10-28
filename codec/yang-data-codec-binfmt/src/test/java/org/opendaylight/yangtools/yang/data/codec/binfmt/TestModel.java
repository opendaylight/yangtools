/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;

public final class TestModel {

    static final QName TEST_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test",
            "2014-03-13", "test");

    static final QName AUG_NAME_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:aug",
            "2014-03-13", "name");

    private static final QName AUG_CONT_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:aug",
            "2014-03-13", "cont");

    static final QName DESC_QNAME = QName.create(TEST_QNAME, "desc");
    static final QName SOME_BINARY_DATA_QNAME = QName.create(TEST_QNAME, "some-binary-data");
    static final QName BINARY_LEAF_LIST_QNAME = QName.create(TEST_QNAME, "binary_leaf_list");
    private static final QName SOME_REF_QNAME = QName.create(TEST_QNAME, "some-ref");
    private static final QName MYIDENTITY_QNAME = QName.create(TEST_QNAME, "myidentity");
    private static final QName SWITCH_FEATURES_QNAME = QName.create(TEST_QNAME, "switch-features");

    private static final QName AUGMENTED_LIST_QNAME = QName.create(TEST_QNAME, "augmented-list");
    static final QName AUGMENTED_LIST_ENTRY_QNAME = QName.create(TEST_QNAME, "augmented-list-entry");

    static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    private static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    static final QName BOOLEAN_LEAF_QNAME = QName.create(TEST_QNAME, "boolean-leaf");
    private static final QName SHORT_LEAF_QNAME = QName.create(TEST_QNAME, "short-leaf");
    private static final QName BYTE_LEAF_QNAME = QName.create(TEST_QNAME, "byte-leaf");
    private static final QName BIGINTEGER_LEAF_QNAME = QName.create(TEST_QNAME, "biginteger-leaf");
    private static final QName BIGDECIMAL_LEAF_QNAME = QName.create(TEST_QNAME, "bigdecimal-leaf");
    static final QName ORDERED_LIST_QNAME = QName.create(TEST_QNAME, "ordered-list");
    static final QName ORDERED_LIST_ENTRY_QNAME = QName.create(TEST_QNAME, "ordered-list-leaf");
    private static final QName UNKEYED_LIST_QNAME = QName.create(TEST_QNAME, "unkeyed-list");
    private static final QName SHOE_QNAME = QName.create(TEST_QNAME, "shoe");
    static final QName ANY_XML_QNAME = QName.create(TEST_QNAME, "any");
    static final QName EMPTY_QNAME = QName.create(TEST_QNAME, "empty-leaf");

    static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    private static final QName TWO_THREE_QNAME = QName.create(TEST_QNAME, "two");
    private static final QName TWO_QNAME = QName.create(TEST_QNAME, "two");

    private static final Integer ONE_ID = 1;
    private static final Integer TWO_ID = 2;
    private static final String TWO_ONE_NAME = "one";
    private static final String TWO_TWO_NAME = "two";
    private static final String DESC = "Hello there";
    private static final Boolean ENABLED = true;
    private static final Short SHORT_ID = 1;
    private static final Byte BYTE_ID = 1;
    // Family specific constants
    private static final QName FAMILY_QNAME = QName.create(
        "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:notification-test", "2014-04-17", "family");
    private static final QName CHILDREN_QNAME = QName.create(FAMILY_QNAME, "children");
    static final QName CHILD_NUMBER_QNAME = QName.create(FAMILY_QNAME, "child-number");
    static final QName CHILD_NAME_QNAME = QName.create(FAMILY_QNAME, "child-name");

    private static final String FIRST_CHILD_NAME = "first child";

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(
            OUTER_LIST_QNAME, ID_QNAME, TWO_ID)
            .withChild(mapNodeBuilder(INNER_LIST_QNAME)
                    .withChild(mapEntry(INNER_LIST_QNAME, NAME_QNAME, TWO_ONE_NAME))
                    .withChild(mapEntry(INNER_LIST_QNAME, NAME_QNAME, TWO_TWO_NAME))
                    .build())
            .build();

    private TestModel() {
        throw new UnsupportedOperationException();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createBaseTestContainerBuilder() {
        // Create a list of shoes
        // This is to test leaf list entry
        final LeafSetEntryNode<Object> nike = ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                new NodeWithValue<>(SHOE_QNAME, "nike")).withValue("nike").build();

        final LeafSetEntryNode<Object> puma = ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                new NodeWithValue<>(SHOE_QNAME, "puma")).withValue("puma").build();

        final SystemLeafSetNode<Object> shoes = ImmutableLeafSetNodeBuilder.create().withNodeIdentifier(
                new NodeIdentifier(SHOE_QNAME)).withChild(nike).withChild(puma).build();

        // Test a leaf-list where each entry contains an identity
        final LeafSetEntryNode<Object> cap1 =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "capability"), DESC_QNAME))
                        .withValue(DESC_QNAME).build();

        final SystemLeafSetNode<Object> capabilities =
                ImmutableLeafSetNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(QName.create(
                                        TEST_QNAME, "capability"))).withChild(cap1).build();

        ContainerNode switchFeatures =
                ImmutableContainerNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(SWITCH_FEATURES_QNAME))
                        .withChild(capabilities).build();

        // Create a leaf list with numbers
        final LeafSetEntryNode<Object> five =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "number"), 5)).withValue(5).build();
        final LeafSetEntryNode<Object> fifteen =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "number"), 15)).withValue(15).build();
        final SystemLeafSetNode<Object> numbers =
                ImmutableLeafSetNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(QName.create(
                                        TEST_QNAME, "number"))).withChild(five).withChild(fifteen)
                        .build();


        // Create augmentations
        MapEntryNode augMapEntry = createAugmentedListEntry(1, "First Test");

        // Create a bits leaf
        NormalizedNodeBuilder<NodeIdentifier, Object, LeafNode<Object>>
                myBits = Builders.leafBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(TEST_QNAME, "my-bits")))
                .withValue(ImmutableSet.of("foo", "bar"));

        // Create unkeyed list entry
        UnkeyedListEntryNode unkeyedListEntry =
                Builders.unkeyedListEntryBuilder().withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
                    UNKEYED_LIST_QNAME)).withChild(ImmutableNodes.leafNode(NAME_QNAME, "unkeyed-entry-name")).build();

        // Create YangInstanceIdentifier with all path arg types.
        YangInstanceIdentifier instanceID = YangInstanceIdentifier.create(
                new NodeIdentifier(QName.create(TEST_QNAME, "qname")),
                NodeIdentifierWithPredicates.of(QName.create(TEST_QNAME, "list-entry"),
                        QName.create(TEST_QNAME, "key"), 10),
                new AugmentationIdentifier(ImmutableSet.of(
                        QName.create(TEST_QNAME, "aug1"), QName.create(TEST_QNAME, "aug2"))),
                new NodeWithValue<>(QName.create(TEST_QNAME, "leaf-list-entry"), "foo"));

        Map<QName, Object> keyValues = new HashMap<>();
        keyValues.put(CHILDREN_QNAME, FIRST_CHILD_NAME);


        // Create the document
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TEST_QNAME))
                .withChild(myBits.build())
                .withChild(ImmutableNodes.leafNode(DESC_QNAME, DESC))
                .withChild(ImmutableNodes.leafNode(BOOLEAN_LEAF_QNAME, ENABLED))
                .withChild(ImmutableNodes.leafNode(SHORT_LEAF_QNAME, SHORT_ID))
                .withChild(ImmutableNodes.leafNode(BYTE_LEAF_QNAME, BYTE_ID))
                .withChild(ImmutableNodes.leafNode(TestModel.BIGINTEGER_LEAF_QNAME, Uint64.valueOf("100")))
                .withChild(ImmutableNodes.leafNode(TestModel.BIGDECIMAL_LEAF_QNAME, Decimal64.valueOf("1.2")))
                .withChild(ImmutableNodes.leafNode(SOME_REF_QNAME, instanceID))
                .withChild(ImmutableNodes.leafNode(MYIDENTITY_QNAME, DESC_QNAME))
                .withChild(Builders.unkeyedListBuilder()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LIST_QNAME))
                        .withChild(unkeyedListEntry).build())
                .withChild(Builders.choiceBuilder()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TWO_THREE_QNAME))
                        .withChild(ImmutableNodes.leafNode(TWO_QNAME, "two")).build())
                .withChild(Builders.orderedMapBuilder()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(ORDERED_LIST_QNAME))
                        .withValue(ImmutableList.<MapEntryNode>builder().add(
                                mapEntryBuilder(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "1").build(),
                                mapEntryBuilder(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "2").build()).build())
                        .build())
                .withChild(shoes)
                .withChild(numbers)
                .withChild(switchFeatures)
                .withChild(mapNodeBuilder(AUGMENTED_LIST_QNAME).withChild(augMapEntry).build())
                .withChild(mapNodeBuilder(OUTER_LIST_QNAME)
                                .withChild(mapEntry(OUTER_LIST_QNAME, ID_QNAME, ONE_ID))
                                .withChild(BAR_NODE).build()
                );
    }

    static ContainerNode createTestContainer() {
        return createBaseTestContainerBuilder().build();
    }

    private static MapEntryNode createAugmentedListEntry(final int id, final String name) {

        Set<QName> childAugmentations = new HashSet<>();
        childAugmentations.add(AUG_CONT_QNAME);

        ContainerNode augCont =
                ImmutableContainerNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new YangInstanceIdentifier.NodeIdentifier(AUG_CONT_QNAME))
                        .withChild(ImmutableNodes.leafNode(AUG_NAME_QNAME, name)).build();


        final YangInstanceIdentifier.AugmentationIdentifier augmentationIdentifier =
                new YangInstanceIdentifier.AugmentationIdentifier(childAugmentations);

        final AugmentationNode augmentationNode =
                Builders.augmentationBuilder()
                        .withNodeIdentifier(augmentationIdentifier).withChild(augCont)
                        .build();

        return ImmutableMapEntryNodeBuilder
                .create()
                .withNodeIdentifier(
                        YangInstanceIdentifier.NodeIdentifierWithPredicates.of(
                                AUGMENTED_LIST_QNAME, ID_QNAME, id))
                .withChild(ImmutableNodes.leafNode(ID_QNAME, id))
                .withChild(augmentationNode).build();
    }
}
