/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NormalizedNodePrettyTreeTest extends AbstractPrettyTreeTest {

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void mapNodePrettyTreeTest() {
        final String expected = String.join("\n",
                "systemMapNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a, value=[",
                "        mapEntryNode{identifier=list-a[{leaf-a=bar}], value=[",
                "            leafNode{identifier=leaf-a, value=bar}",
                "            systemMapNode{identifier=list-b, value=[",
                "                mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                    leafNode{identifier=leaf-b, value=two}]}",
                "                mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                    leafNode{identifier=leaf-b, value=one}]}]}]}",
                "        mapEntryNode{identifier=list-a[{leaf-a=foo}], value=[",
                "            leafNode{identifier=leaf-a, value=foo}]}]}");
        assertEquals(expected, createMapNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void mapEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "mapEntryNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a=bar}], value=[",
                "        leafNode{identifier=leaf-a, value=bar}",
                "        systemMapNode{identifier=list-b, value=[",
                "            mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                leafNode{identifier=leaf-b, value=two}]}",
                "            mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                leafNode{identifier=leaf-b, value=one}]}]}]}");
        assertEquals(expected, createMapEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void choicePrettyTreeTest() {
        final String expected = String.join("\n",
                "choiceNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)choice, value=[",
                "        augmentationNode{identifier=AugmentationIdentifier{childNames=[augment]}, value=[",
                "            leafNode{identifier=augment, value=Augmented leaf value}]}]}");
        assertEquals(expected, createChoiceNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void augmentationPrettyTreeTest() {
        final String expected = String.join("\n",
                "augmentationNode{",
                "    identifier=AugmentationIdentifier{childNames=[(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)augment]}, value=[",
                "        leafNode{identifier=augment, value=Augmented leaf value}]}");
        assertEquals(expected, createAugmentationNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafPrettyTreeTest() {
        final String expected = String.join("\n",
                "leafNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf, value=Leaf value}");
        assertEquals(expected, createLeafNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void leafSetPrettyTreeTest() {
        final String expected = String.join("\n",
                "systemLeafSetNode{" ,
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-set, value=[",
                "        leafSetEntryNode{identifier=leaf-set[Leaf set value], value=Leaf set value}]}");
        assertEquals(expected, createLeafSetNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userLeafSetPrettyTreeTest() {
        final String expected = String.join("\n",
                "userLeafSetNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-leaf-set, value=[",
                "        leafSetEntryNode{identifier=user-leaf-set[User leaf set value], value=User leaf set value}]}");
        assertEquals(expected, createUserLeafSetNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapPrettyTreeTest() {
        final String expected = String.join("\n",
                "userMapNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-map, value=[",
                "        mapEntryNode{identifier=user-map[{user-map-entry=User map entry value}], value=[",
                "            leafNode{identifier=user-map-entry, value=User map entry value}]}]}");
        assertEquals(expected, createUserMapNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void userMapEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "mapEntryNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-map[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)user-map-entry=User map entry value}], value=[",
                "        leafNode{identifier=user-map-entry, value=User map entry value}]}");
        assertEquals(expected, createUserMapEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListPrettyTreeTest() {
        final String expected = String.join("\n",
                "unkeyedListNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)unkeyed-list, value=[",
                "        unkeyedListEntryNode{identifier=unkeyed-list-entry, value=[",
                "            leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}]}");
        assertEquals(expected, createUnkeyedListNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void unkeyedListEntryPrettyTreeTest() {
        final String expected = String.join("\n",
                "unkeyedListEntryNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)unkeyed-list-entry, value=[",
                "        leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}");
        assertEquals(expected, createUnkeyedListEntryNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void anyDataPrettyTreeTest() {
        final String expected = String.join("\n",
                "anydataNode{",
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)any-data, value=Any data value}");
        assertEquals(expected, createAnyDataNode().prettyTree().get());
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void containerPrettyTreeTest() {
        final String expected = String.join("\n",
                "containerNode{" ,
                "    identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, value=[",
                "        userMapNode{identifier=user-map, value=[",
                "            mapEntryNode{identifier=user-map[{user-map-entry=User map entry value}], value=[",
                "                leafNode{identifier=user-map-entry, value=User map entry value}]}]}",
                "        userLeafSetNode{identifier=user-leaf-set, value=[",
                "            leafSetEntryNode{identifier=user-leaf-set[User leaf set value], value=User leaf set value}]}",
                "        systemMapNode{identifier=list-a, value=[",
                "            mapEntryNode{identifier=list-a[{leaf-a=bar}], value=[",
                "                leafNode{identifier=leaf-a, value=bar}",
                "                systemMapNode{identifier=list-b, value=[",
                "                    mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                        leafNode{identifier=leaf-b, value=two}]}",
                "                    mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                        leafNode{identifier=leaf-b, value=one}]}]}]}",
                "            mapEntryNode{identifier=list-a[{leaf-a=foo}], value=[",
                "                leafNode{identifier=leaf-a, value=foo}]}]}",
                "        containerNode{identifier=(urn:opendaylight:controller:sal:dom:store:another)another, value=[",
                "            systemMapNode{identifier=list-from-another-namespace, value=[",
                "                mapEntryNode{identifier=list-from-another-namespace[{leaf-from-another-namespace=Leaf from another namespace value}], value=[",
                "                    leafNode{identifier=leaf-from-another-namespace, value=Leaf from another namespace value}]}]}]}",
                "        choiceNode{identifier=choice, value=[",
                "            augmentationNode{identifier=AugmentationIdentifier{childNames=[augment]}, value=[",
                "                leafNode{identifier=augment, value=Augmented leaf value}]}]}",
                "        anydataNode{identifier=any-data, value=Any data value}",
                "        unkeyedListNode{identifier=unkeyed-list, value=[",
                "            unkeyedListEntryNode{identifier=unkeyed-list-entry, value=[",
                "                leafNode{identifier=unkeyed-list-leaf, value=Unkeyed list leaf value}]}]}",
                "        leafNode{identifier=leaf, value=Leaf value}",
                "        systemLeafSetNode{identifier=leaf-set, value=[",
                "            leafSetEntryNode{identifier=leaf-set[Leaf set value], value=Leaf set value}]}]}");

        assertEquals(expected, createContainerNode().prettyTree().get());
    }
}
