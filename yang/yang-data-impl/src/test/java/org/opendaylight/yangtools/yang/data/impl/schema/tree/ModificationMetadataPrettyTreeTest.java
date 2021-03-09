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

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
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
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.tree.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.spi.tree.Version;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ModificationMetadataPrettyTreeTest {
    private static final QName ROOT_QNAME = QName.create(
            "urn:opendaylight:controller:sal:dom:store:test", "2014-03-13", "root");
    private static final YangInstanceIdentifier ROOT_YII = YangInstanceIdentifier.of(ROOT_QNAME);

    private static final QName ANOTHER_QNAME = QName.create(
            "urn:opendaylight:controller:sal:dom:store:another", "another");

    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
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
    private static final QName USER_ORDERED_MAP_ENTRY_QNAME = QName.create(ROOT_QNAME, "user-map-entry");

    private static final QName UNKEYED_LIST_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list");
    private static final QName UNKEYED_LIST_ENTRY_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-entry");
    private static final QName UNKEYED_LIST_LEAF_QNAME = QName.create(ROOT_QNAME,
            "unkeyed-list-leaf");

    private static final QName ANY_DATA_QNAME = QName.create(ROOT_QNAME, "any-data");

    private static EffectiveModelContext SCHEMA_CONTEXT;
    private static RootApplyStrategy ROOT_OPER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/pretty-print/");
        ROOT_OPER = RootApplyStrategy.from(SchemaAwareApplyOperation.from(SCHEMA_CONTEXT,
                DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

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
    private static NormalizedNode createContainerNode() {
        return ImmutableContainerNodeBuilder.create()
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

    private static MapNode createMapNode() {
        return mapNodeBuilder(LIST_A_QNAME)
                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, "foo"))
                .withChild(createMapEntryNode()).build();
    }

    private static MapEntryNode createMapEntryNode() {
        return mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, "bar")
                .withChild(mapNodeBuilder(LIST_B_QNAME)
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, "one"))
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, "two"))
                        .build()).build();
    }

    private static ChoiceNode createChoiceNode() {
        return Builders.choiceBuilder()
                .withNodeIdentifier(NodeIdentifier.create(CHOICE_QNAME))
                .withChild(createAugmentationNode())
                .build();
    }

    private static ContainerNode createContainerFromAnotherNamespace() {
        return ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(ANOTHER_QNAME))
                .withChild(mapNodeBuilder(LIST_ANOTHER_NAMESPACE_QNAME)
                        .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME,
                                LEAF_ANOTHER_NAMESPACE_QNAME,
                                "Leaf from another namespace value"))
                        .build())
                .build();
    }

    private static LeafNode<String> createAugmentationNode() {
        return leafNode(AUGMENT_QNAME, "Augmented leaf value");
    }

    private static LeafNode<String> createLeafNode() {
        return Builders.<String>leafBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_QNAME))
                .withValue("Leaf value")
                .build();
    }

    private static LeafSetNode<String> createLeafSetNode() {
        final String value = "Leaf set value";
        final LeafSetEntryNode<String> leafSetValue = Builders.<String>leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(LEAF_SET_QNAME, value))
                .withValue(value)
                .build();
        return Builders.<String>leafSetBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_SET_QNAME))
                .withValue(List.of(leafSetValue))
                .build();
    }

    private static UserLeafSetNode<String> createUserLeafSetNode() {
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

    private static UserMapNode createUserMapNode() {
        return Builders.orderedMapBuilder()
                .withNodeIdentifier(NodeIdentifier.create(USER_ORDERED_MAP_QNAME))
                .withValue(List.of(createUserMapEntryNode()))
                .build();
    }

    private static MapEntryNode createUserMapEntryNode() {
        return mapEntry(USER_ORDERED_MAP_QNAME,
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
    public void mapNodeModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(LIST_A_QNAME)
                        .build(), createMapNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    list-a=ModifiedNode{",
                "                        identifier=list-a, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void mapEntryNodeModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(LIST_A_QNAME)
                .nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, "bar").build(), createMapEntryNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    list-a=ModifiedNode{",
                "                        identifier=list-a, operation=TOUCH, childModification={",
                "                            list-a[{leaf-a=bar}]=ModifiedNode{",
                "                                identifier=list-a[{leaf-a=bar}], operation=WRITE}}}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void choiceModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(CHOICE_QNAME)
                .build(), createChoiceNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    choice=ModifiedNode{",
                "                        identifier=choice, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void anotherNamespaceModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(ANOTHER_QNAME)
                .build(), createContainerFromAnotherNamespace());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    (urn:opendaylight:controller:sal:dom:store:another)another=ModifiedNode{",
                "                        identifier=(urn:opendaylight:controller:sal:dom:store:another)another, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void augmentationModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(CHOICE_QNAME).node(AUGMENT_QNAME).build(), createAugmentationNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    choice=ModifiedNode{",
                "                        identifier=choice, operation=TOUCH, childModification={",
                "                            augment=ModifiedNode{",
                "                                identifier=augment, operation=WRITE}}}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(LEAF_QNAME)
                .build(), createLeafNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    leaf=ModifiedNode{",
                "                        identifier=leaf, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafSetModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII).node(LEAF_SET_QNAME)
                .build(), createLeafSetNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    leaf-set=ModifiedNode{",
                "                        identifier=leaf-set, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userLeafSetModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(USER_LEAF_SET_QNAME).build(), createUserLeafSetNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    user-leaf-set=ModifiedNode{",
                "                        identifier=user-leaf-set, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(USER_ORDERED_MAP_QNAME).build(), createUserMapNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    user-map=ModifiedNode{",
                "                        identifier=user-map, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapEntryModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                        .node(USER_ORDERED_MAP_QNAME)
                        .nodeWithKey(USER_ORDERED_MAP_QNAME, USER_ORDERED_MAP_ENTRY_QNAME, "User map entry value").build(),
                createUserMapEntryNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    user-map=ModifiedNode{",
                "                        identifier=user-map, operation=TOUCH, childModification={",
                "                            user-map[{user-map-entry=User map entry value}]=ModifiedNode{",
                "                                identifier=user-map[{user-map-entry=User map entry value}], operation=WRITE}}}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(UNKEYED_LIST_QNAME).build(), createUnkeyedListNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    unkeyed-list=ModifiedNode{",
                "                        identifier=unkeyed-list, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListEntryModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(UNKEYED_LIST_QNAME).node(UNKEYED_LIST_ENTRY_QNAME).build(),
                createUnkeyedListEntryNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    unkeyed-list=ModifiedNode{",
                "                        identifier=unkeyed-list, operation=TOUCH, childModification={",
                "                            unkeyed-list-entry=ModifiedNode{",
                "                                identifier=unkeyed-list-entry, operation=WRITE}}}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void anyDataModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(ANY_DATA_QNAME).build(), createAnyDataNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=TOUCH, childModification={",
                "                    any-data=ModifiedNode{",
                "                        identifier=any-data, operation=WRITE}}}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void containerModificationPrettyTreeTest() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(ROOT_YII, createContainerNode());

        final String expected = String.join("\n",
                "MutableDataTree [",
                "    modification=ModifiedNode{",
                "        identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, operation=TOUCH, childModification={",
                "            root=ModifiedNode{",
                "                identifier=root, operation=WRITE}}}]");

        assertEquals(expected, modificationTree.prettyTree().get());
    }
}
