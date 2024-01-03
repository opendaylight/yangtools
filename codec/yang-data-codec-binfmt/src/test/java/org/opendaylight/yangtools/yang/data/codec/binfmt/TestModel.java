/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

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
    static final QName CHILD_NUMBER_QNAME = QName.create(FAMILY_QNAME, "child-number");
    static final QName CHILD_NAME_QNAME = QName.create(FAMILY_QNAME, "child-name");

    private static final MapEntryNode BAR_NODE = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(OUTER_LIST_QNAME, ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.leafNode(ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(INNER_LIST_QNAME))
            .withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(INNER_LIST_QNAME, NAME_QNAME, TWO_ONE_NAME))
                .withChild(ImmutableNodes.leafNode(NAME_QNAME, TWO_ONE_NAME))
                .build())
            .withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(INNER_LIST_QNAME, NAME_QNAME, TWO_TWO_NAME))
                .withChild(ImmutableNodes.leafNode(NAME_QNAME, TWO_TWO_NAME))
                .build())
            .build())
        .build();

    private TestModel() {
        throw new UnsupportedOperationException();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createBaseTestContainerBuilder() {
        // Create the document
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TEST_QNAME))
            // Create a bits leaf
            .withChild(ImmutableNodes.leafNode(QName.create(TEST_QNAME, "my-bits"), ImmutableSet.of("foo", "bar")))
            .withChild(ImmutableNodes.leafNode(DESC_QNAME, DESC))
            .withChild(ImmutableNodes.leafNode(BOOLEAN_LEAF_QNAME, ENABLED))
            .withChild(ImmutableNodes.leafNode(SHORT_LEAF_QNAME, SHORT_ID))
            .withChild(ImmutableNodes.leafNode(BYTE_LEAF_QNAME, BYTE_ID))
            .withChild(ImmutableNodes.leafNode(TestModel.BIGINTEGER_LEAF_QNAME, Uint64.valueOf(100)))
            .withChild(ImmutableNodes.leafNode(TestModel.BIGDECIMAL_LEAF_QNAME, Decimal64.valueOf("1.2")))
            .withChild(ImmutableNodes.leafNode(SOME_REF_QNAME,
                // Create YangInstanceIdentifier with all path arg types.
                YangInstanceIdentifier.of(new NodeIdentifier(QName.create(TEST_QNAME, "qname")),
                    NodeIdentifierWithPredicates.of(QName.create(TEST_QNAME, "list-entry"),
                        QName.create(TEST_QNAME, "key"), 10),
                    new NodeWithValue<>(QName.create(TEST_QNAME, "leaf-list-entry"), "foo"))))
            .withChild(ImmutableNodes.leafNode(MYIDENTITY_QNAME, DESC_QNAME))
            .withChild(ImmutableNodes.newUnkeyedListBuilder()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
                // Create unkeyed list entry
                .withChild(ImmutableNodes.newUnkeyedListEntryBuilder()
                    .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
                    .withChild(ImmutableNodes.leafNode(NAME_QNAME, "unkeyed-entry-name"))
                    .build())
                .build())
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(TWO_THREE_QNAME))
                .withChild(ImmutableNodes.leafNode(TWO_QNAME, "two"))
                .build())
            .withChild(ImmutableNodes.newUserMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(ORDERED_LIST_QNAME))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "1"))
                    .withChild(ImmutableNodes.leafNode(ORDERED_LIST_ENTRY_QNAME, "1"))
                    .build())
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "2"))
                    .withChild(ImmutableNodes.leafNode(ORDERED_LIST_ENTRY_QNAME, "2"))
                    .build())
                .build())
            // Create a list of shoes
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(SHOE_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(SHOE_QNAME, "nike"))
                .withChild(ImmutableNodes.leafSetEntry(SHOE_QNAME, "puma"))
                .build())
            // Create a leaf list with numbers
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(TEST_QNAME, "number")))
                .withChild(ImmutableNodes.leafSetEntry(QName.create(TEST_QNAME, "number"), 5))
                .withChild(ImmutableNodes.leafSetEntry(QName.create(TEST_QNAME, "number"), 15))
                .build())
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SWITCH_FEATURES_QNAME))
                // Test a leaf-list where each entry contains an identity
                .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TEST_QNAME, "capability")))
                    .withChild(ImmutableNodes.leafSetEntry(QName.create(TEST_QNAME, "capability"), DESC_QNAME))
                    .build())
                .build())
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(AUGMENTED_LIST_QNAME))
                // Create augmentations
                .withChild(createAugmentedListEntry(1, "First Test"))
                .build())
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(OUTER_LIST_QNAME))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(OUTER_LIST_QNAME, ID_QNAME, ONE_ID))
                    .withChild(ImmutableNodes.leafNode(ID_QNAME, ONE_ID))
                    .build())
                .withChild(BAR_NODE)
                .build());
    }

    static ContainerNode createTestContainer() {
        return createBaseTestContainerBuilder().build();
    }

    private static MapEntryNode createAugmentedListEntry(final int id, final String name) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(AUGMENTED_LIST_QNAME, ID_QNAME, id))
            .withChild(ImmutableNodes.leafNode(ID_QNAME, id))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(AUG_CONT_QNAME))
                .withChild(ImmutableNodes.leafNode(AUG_NAME_QNAME, name))
                .build())
            .build();
    }
}
