/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.spi.tree.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.spi.tree.Version;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class InMemoryDataTreeModificationPrettyTreeTest extends AbstractPrettyTreeTest {
    private static final YangInstanceIdentifier ROOT_YII = YangInstanceIdentifier.of(ROOT_QNAME);

    private static EffectiveModelContext SCHEMA_CONTEXT;
    private static RootApplyStrategy ROOT_OPER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/pretty-print/");
        ROOT_OPER = RootApplyStrategy.from(SchemaAwareApplyOperation.from(SCHEMA_CONTEXT,
                DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void testMapNodeModificationPrettyTree() {
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
    public void testMapEntryNodeModificationPrettyTree() {
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
    public void testChoiceModificationPrettyTree() {
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
    public void testAnotherNamespaceModificationPrettyTree() {
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
    public void testAugmentationModificationPrettyTree() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(CHOICE_QNAME).node(AUGMENT_QNAME).build(), createAugmentedLeafNode());

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
    public void testLeafModificationPrettyTree() {
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
    public void testLeafSetModificationPrettyTree() {
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
    public void testUserLeafSetModificationPrettyTree() {
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
    public void testUserMapModificationPrettyTree() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                .node(USER_MAP_QNAME).build(), createUserMapNode());

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
    public void testUserMapEntryModificationPrettyTree() {
        final InMemoryDataTreeModification modificationTree = new InMemoryDataTreeModification(
                new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                        TreeNodeFactory.createTreeNode(createContainerNode(), Version.initial()),
                        ROOT_OPER), ROOT_OPER);
        modificationTree.write(YangInstanceIdentifier.builder(ROOT_YII)
                        .node(USER_MAP_QNAME)
                        .nodeWithKey(USER_MAP_QNAME, USER_MAP_ENTRY_QNAME, "User map entry value").build(),
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
    public void testUnkeyedListModificationPrettyTree() {
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
    public void testUnkeyedListEntryModificationPrettyTree() {
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
    public void testAnyDataModificationPrettyTree() {
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
    public void testContainerModificationPrettyTree() {
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
