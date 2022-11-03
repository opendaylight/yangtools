/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public abstract class AbstractPrettyTreeTest {
    protected static final QName ROOT_QNAME = QName.create(
            "urn:opendaylight:controller:sal:dom:store:test", "2014-03-13", "root");
    protected static final QName ANOTHER_QNAME = QName.create(
            "urn:opendaylight:controller:sal:dom:store:another", "another");

    protected static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    protected static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    protected static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    protected static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");

    protected static final QName CHOICE_QNAME = QName.create(ROOT_QNAME, "choice");
    protected static final QName AUGMENT_QNAME = QName.create(ROOT_QNAME, "augment");

    protected static final QName LIST_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_QNAME,
            "list-from-another-namespace");
    protected static final QName LEAF_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_QNAME,
            "leaf-from-another-namespace");

    protected static final QName LEAF_QNAME = QName.create(ROOT_QNAME, "leaf");
    protected static final QName LEAF_SET_QNAME = QName.create(ROOT_QNAME, "leaf-set");

    protected static final QName USER_LEAF_SET_QNAME = QName.create(ROOT_QNAME, "user-leaf-set");
    protected static final QName USER_MAP_QNAME = QName.create(ROOT_QNAME, "user-map");
    protected static final QName USER_MAP_ENTRY_QNAME = QName.create(ROOT_QNAME, "user-map-entry");

    protected static final QName UNKEYED_LIST_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list");
    protected static final QName UNKEYED_LIST_ENTRY_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-entry");
    protected static final QName UNKEYED_LIST_LEAF_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-leaf");

    protected static final QName ANY_DATA_QNAME = QName.create(ROOT_QNAME, "any-data");

    /**
     * Return a test node.
     *
     * <pre>
     * root
     *     list-a
     *          leaf-a "foo"
     *     list-a
     *          leaf-a "bar"
     *          list-b
     *                  leaf-b "one"
     *          list-b
     *                  leaf-b "two"
     *     choice
     *          augment
     *                  augmented-leaf "Augmented leaf value"
     *     another
     *          list-from-another-namespace
     *               leaf-from-another-namespace "Leaf from another namespace value"
     *     leaf "Leaf value"
     *     leaf-set "Leaf set value"
     *     user-leaf-set "User leaf set value"
     *     user-map
     *          user-map-entry "User map entry value"
     *     unkeyed-list
     *          unkeyed-list-entry
     *               unkeyed-list-leaf "Unkeyed list leaf value"
     *     any-data "Any data value"
     *
     * </pre>
     *
     * @return A test node
     */
    protected static ContainerNode createContainerNode() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
            .withChild(createMapNode())
            .withChild(createChoiceNode())
            .withChild(createContainerFromAnotherNamespace())
            .withChild(createLeafNode())
            .withChild(createLeafSetNode())
            .withChild(createUserLeafSetNode())
            .withChild(createUserMapNode())
            .withChild(createUnkeyedListNode())
            .withChild(createAnyDataNode())
            .build();
    }

    protected static MapNode createMapNode() {
        return mapNodeBuilder(LIST_A_QNAME)
                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, "foo"))
                .withChild(createMapEntryNode()).build();
    }

    protected static MapEntryNode createMapEntryNode() {
        return mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, "bar")
                .withChild(mapNodeBuilder(LIST_B_QNAME)
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, "one"))
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, "two"))
                        .build()).build();
    }

    protected static ChoiceNode createChoiceNode() {
        return Builders.choiceBuilder()
                .withNodeIdentifier(NodeIdentifier.create(CHOICE_QNAME))
                .withChild(createAugmentationNode())
                .build();
    }

    protected static AugmentationNode createAugmentationNode() {
        return Builders.augmentationBuilder()
                .withNodeIdentifier(AugmentationIdentifier
                        .create(Set.of(AUGMENT_QNAME)))
                .withChild(createAugmentedLeafNode())
                .build();
    }

    protected static LeafNode<String> createAugmentedLeafNode() {
        return leafNode(AUGMENT_QNAME, "Augmented leaf value");
    }

    protected static ContainerNode createContainerFromAnotherNamespace() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ANOTHER_QNAME))
            .withChild(mapNodeBuilder(LIST_ANOTHER_NAMESPACE_QNAME)
                .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME, LEAF_ANOTHER_NAMESPACE_QNAME,
                    "Leaf from another namespace value"))
                .build())
            .build();
    }

    protected static LeafNode<String> createLeafNode() {
        return ImmutableNodes.leafNode(LEAF_QNAME, "Leaf value");
    }

    protected static LeafSetNode<String> createLeafSetNode() {
        final String value = "Leaf set value";
        return Builders.<String>leafSetBuilder()
            .withNodeIdentifier(NodeIdentifier.create(LEAF_SET_QNAME))
            .withValue(List.of(Builders.<String>leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(LEAF_SET_QNAME, value))
                .withValue(value)
                .build()))
            .build();
    }

    protected static UserLeafSetNode<String> createUserLeafSetNode() {
        final String value = "User leaf set value";
        final LeafSetEntryNode<String> leafSetValue = Builders.<String>leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(USER_LEAF_SET_QNAME, value))
                .withValue(value)
                .build();
        return Builders.<String>orderedLeafSetBuilder()
                .withNodeIdentifier(NodeIdentifier.create(USER_LEAF_SET_QNAME))
                .withValue(List.of(leafSetValue))
                .build();
    }

    protected static UserMapNode createUserMapNode() {
        return Builders.orderedMapBuilder()
                .withNodeIdentifier(NodeIdentifier.create(USER_MAP_QNAME))
                .withValue(List.of(createUserMapEntryNode()))
                .build();
    }

    protected static MapEntryNode createUserMapEntryNode() {
        return mapEntry(USER_MAP_QNAME, USER_MAP_ENTRY_QNAME, "User map entry value");
    }

    protected static UnkeyedListNode createUnkeyedListNode() {
        return Builders.unkeyedListBuilder()
                .withNodeIdentifier(NodeIdentifier.create(UNKEYED_LIST_QNAME))
                .withChild(createUnkeyedListEntryNode())
                .build();
    }

    protected static UnkeyedListEntryNode createUnkeyedListEntryNode() {
        return Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(NodeIdentifier.create(UNKEYED_LIST_ENTRY_QNAME))
                .withChild(leafNode(UNKEYED_LIST_LEAF_QNAME, "Unkeyed list leaf value"))
                .build();
    }

    protected static AnydataNode<String> createAnyDataNode() {
        return Builders.anydataBuilder(String.class)
                .withNodeIdentifier(NodeIdentifier.create(ANY_DATA_QNAME))
                .withValue("Any data value")
                .build();
    }
}
