/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class NormalizedNodePrettyTreeTest {
    private static final QName ROOT_QNAME = QName.create(
        "urn:opendaylight:controller:sal:dom:store:test", "2014-03-13", "root");
    private static final QName ANOTHER_QNAME = QName.create(
        "urn:opendaylight:controller:sal:dom:store:another", "another");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final QName CHOICE_QNAME = QName.create(ROOT_QNAME, "choice");
    private static final QName AUGMENT_QNAME = QName.create(ROOT_QNAME, "augment");
    private static final QName LIST_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_QNAME,
            "list-from-another-namespace");
    private static final QName LEAF_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_QNAME,
        "leaf-from-another-namespace");
    private static final QName LEAF_QNAME = QName.create(ROOT_QNAME, "leaf");
    private static final QName LEAF_SET_QNAME = QName.create(ROOT_QNAME, "leaf-set");
    private static final QName USER_LEAF_SET_QNAME = QName.create(ROOT_QNAME, "user-leaf-set");
    private static final QName USER_ORDERED_MAP_QNAME = QName.create(ROOT_QNAME, "user-map");
    private static final QName USER_ORDERED_MAP_ENTRY_QNAME = QName.create(ROOT_QNAME, "use-map-entry");
    private static final QName UNKEYED_LIST_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list");
    private static final QName UNKEYED_LIST_ENTRY_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-entry");
    private static final QName UNKEYED_LIST_LEAF_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-leaf");
    private static final QName ANY_DATA_QNAME = QName.create(ROOT_QNAME, "any-data");

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String ONE = "one";
    private static final String TWO = "two";

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
     *               leaf "Leaf value"
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
    private static NormalizedNode createContainerNode() {
        return ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
                .withChild(createMapNode())
                .withChild(createChoiceNode())
                .withChild(ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new NodeIdentifier(ANOTHER_QNAME))
                        .withChild(mapNodeBuilder(LIST_ANOTHER_NAMESPACE_QNAME)
                                .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME,
                                        LEAF_ANOTHER_NAMESPACE_QNAME,
                                        "Leaf from another namespace value"))
                                .build())
                        .build())
                .withChild(createLeafNode())
                .withChild(createLeafSetNode())
                .withChild(createUserLeafSetNode())
                .withChild(createUserMapNode())
                .withChild(createUnkeyedListNode())
                .withChild(createAnyDataNode())
                .build();
    }

    private static MapNode createMapNode() {
        return mapNodeBuilder(LIST_A_QNAME)
                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                .withChild(createMapEntryNode()).build();
    }

    private static MapEntryNode createMapEntryNode() {
        return mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR)
                .withChild(mapNodeBuilder(LIST_B_QNAME)
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO))
                        .build()).build();
    }

    private static ChoiceNode createChoiceNode() {
        return Builders.choiceBuilder()
                .withNodeIdentifier(NodeIdentifier.create(CHOICE_QNAME))
                .withChild(createAugmentationNode())
                .build();
    }

    private static AugmentationNode createAugmentationNode() {
        return Builders.augmentationBuilder()
                .withNodeIdentifier(AugmentationIdentifier
                        .create(Sets.newHashSet(AUGMENT_QNAME)))
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(AUGMENT_QNAME))
                        .withValue("Augmented leaf value")
                        .build())
                .build();
    }

    private static LeafNode<String> createLeafNode() {
        return Builders.<String>leafBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_QNAME))
                .withValue("Leaf value")
                .build();
    }

    private static LeafSetNode<String> createLeafSetNode() {
        final LeafSetEntryNode<String> leafSetValue = Builders.<String>leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(LEAF_SET_QNAME, "Leaf set value"))
                .withValue("Leaf set value")
                .build();
        return Builders.<String>leafSetBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_SET_QNAME))
                .withValue(List.of(leafSetValue))
                .build();
    }

    private static UserLeafSetNode<String> createUserLeafSetNode() {
        final LeafSetEntryNode<String> leafSetValue = Builders.<String>leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue(USER_LEAF_SET_QNAME, "User leaf set value"))
                .withValue("User leaf set value")
                .build();
        return Builders.<String>orderedLeafSetBuilder()
                .withNodeIdentifier(NodeIdentifier.create(USER_LEAF_SET_QNAME))
                .withValue(List.of(leafSetValue))
                .build();
    }

    private static UserMapNode createUserMapNode() {
        return Builders.orderedMapBuilder()
                .withNodeIdentifier(NodeIdentifier.create(USER_ORDERED_MAP_QNAME))
                .withValue(List.of(createUserMapEntryNode()))
                .build();
    }

    private static MapEntryNode createUserMapEntryNode() {
        return mapEntry(USER_ORDERED_MAP_ENTRY_QNAME,
                USER_ORDERED_MAP_ENTRY_QNAME, "User map entry value");
    }

    private static UnkeyedListNode createUnkeyedListNode() {
        return Builders.unkeyedListBuilder()
                .withNodeIdentifier(NodeIdentifier.create(UNKEYED_LIST_QNAME))
                .withChild(createUnkeyedListEntryNode())
                .build();
    }

    private static UnkeyedListEntryNode createUnkeyedListEntryNode() {
        return Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(NodeIdentifier.create(UNKEYED_LIST_ENTRY_QNAME))
                .withChild(leafNode(UNKEYED_LIST_LEAF_QNAME, "Unkeyed list leaf value"))
                .build();
    }

    private static AnydataNode<String> createAnyDataNode() {
        return Builders.anydataBuilder(String.class)
                .withNodeIdentifier(NodeIdentifier.create(ANY_DATA_QNAME))
                .withValue("Any data value")
                .build();
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void mapNodePrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "systemMapNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a, value=[",
                "    mapEntryNode{identifier=list-a[{leaf-a=bar}], value=[",
                "        leafNode{identifier=leaf-a, value=bar}",
                "        systemMapNode{identifier=list-b, value=[",
                "            mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                leafNode{identifier=leaf-b, value=two}]}",
                "            mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                leafNode{identifier=leaf-b, value=one}]}]}]}",
                "    mapEntryNode{identifier=list-a[{leaf-a=foo}], value=[",
                "        leafNode{identifier=leaf-a, value=foo}]}]}");
        assertEquals(expected, createMapNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void mapEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a=bar}], value=[",
                "    leafNode{identifier=leaf-a, value=bar}",
                "    systemMapNode{identifier=list-b, value=[",
                "        mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "            leafNode{identifier=leaf-b, value=two}]}",
                "        mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "            leafNode{identifier=leaf-b, value=one}]}]}]}");
        assertEquals(expected, createMapEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void choicePrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "choiceNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)choice, value=[",
                "    augmentationNode{identifier=AugmentationIdentifier{childNames=[augment]}, value=[",
                "        leafNode{identifier=augment, value=Augmented leaf value}]}]}");
        assertEquals(expected, createChoiceNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void augmentationPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "augmentationNode{identifier=AugmentationIdentifier{childNames=[(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)augment]}, value=[",
                "    leafNode{identifier=augment, value=Augmented leaf value}]}");
        assertEquals(expected, createAugmentationNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf, value=Leaf value}");
        assertEquals(expected, createLeafNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafSetPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "systemLeafSetNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-set, value=[",
                "    leafSetEntryNode{identifier=leaf-set[Leaf set value], value=Leaf set value}]}");
        assertEquals(expected, createLeafSetNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userLeafSetPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "userLeafSetNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-leaf-set, value=[",
                "    leafSetEntryNode{identifier=user-leaf-set[User leaf set value], value=User leaf set value}]}");
        assertEquals(expected, createUserLeafSetNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "userMapNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-map, value=[",
                "    mapEntryNode{identifier=use-map-entry[{use-map-entry=User map entry value}], value=[",
                "        leafNode{identifier=use-map-entry, value=User map entry value}]}]}");
        assertEquals(expected, createUserMapNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)use-map-entry[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)use-map-entry=User map entry value}], value=[",
                "    leafNode{identifier=use-map-entry, value=User map entry value}]}");
        assertEquals(expected, createUserMapEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "unkeyedListNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)unkeyed-list, value=[",
                "    unkeyedListEntryNode{identifier=unkeyed-list-entry, value=[",
                "        leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}]}");
        assertEquals(expected, createUnkeyedListNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "unkeyedListEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)unkeyed-list-entry, value=[",
                "    leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}");
        assertEquals(expected, createUnkeyedListEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void anyDataPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "anydataNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)any-data, value=Any data value}");
        assertEquals(expected, createAnyDataNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void containerPrettyTreeTest() {
        final String expected = String.join("\n",
                "",
                "containerNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, value=[",
                "    userMapNode{identifier=user-map, value=[",
                "        mapEntryNode{identifier=use-map-entry[{use-map-entry=User map entry value}], value=[",
                "            leafNode{identifier=use-map-entry, value=User map entry value}]}]}",
                "    userLeafSetNode{identifier=user-leaf-set, value=[",
                "        leafSetEntryNode{identifier=user-leaf-set[User leaf set value], value=User leaf set value}]}",
                "    systemMapNode{identifier=list-a, value=[",
                "        mapEntryNode{identifier=list-a[{leaf-a=bar}], value=[",
                "            leafNode{identifier=leaf-a, value=bar}",
                "            systemMapNode{identifier=list-b, value=[",
                "                mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                    leafNode{identifier=leaf-b, value=two}]}",
                "                mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                    leafNode{identifier=leaf-b, value=one}]}]}]}",
                "        mapEntryNode{identifier=list-a[{leaf-a=foo}], value=[",
                "            leafNode{identifier=leaf-a, value=foo}]}]}",
                "    containerNode{identifier=(urn:opendaylight:controller:sal:dom:store:another)another, value=[",
                "        systemMapNode{identifier=list-from-another-namespace, value=[",
                "            mapEntryNode{identifier=list-from-another-namespace[{leaf-from-another-namespace=Leaf from another namespace value}], value=[",
                "                leafNode{identifier=leaf-from-another-namespace, value=Leaf from another namespace value}]}]}]}",
                "    choiceNode{identifier=choice, value=[",
                "        augmentationNode{identifier=AugmentationIdentifier{childNames=[augment]}, value=[",
                "            leafNode{identifier=augment, value=Augmented leaf value}]}]}",
                "    anydataNode{identifier=any-data, value=Any data value}",
                "    unkeyedListNode{identifier=unkeyed-list, value=[",
                "        unkeyedListEntryNode{identifier=unkeyed-list-entry, value=[",
                "            leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}]}",
                "    leafNode{identifier=leaf, value=Leaf value}",
                "    systemLeafSetNode{identifier=leaf-set, value=[",
                "        leafSetEntryNode{identifier=leaf-set[Leaf set value], value=Leaf set value}]}]}");

        assertEquals(expected, createContainerNode().prettyTree().get());
    }
}
