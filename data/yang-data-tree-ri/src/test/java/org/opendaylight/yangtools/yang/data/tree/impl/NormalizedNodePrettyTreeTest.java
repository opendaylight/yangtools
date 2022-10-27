/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NormalizedNodePrettyTreeTest extends AbstractPrettyTreeTest {
    @Test
    public void testMapNodePrettyTree() {
        assertEquals(String.join("\n",
            "systemMapNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)list-a = {",
            "    mapEntryNode list-a = {",
            "        leafNode leaf-a = \"bar\"",
            "        systemMapNode list-b = {",
            "            mapEntryNode list-b = {",
            "                leafNode leaf-b = \"two\"",
            "            }",
            "            mapEntryNode list-b = {",
            "                leafNode leaf-b = \"one\"",
            "            }",
            "        }",
            "    }",
            "    mapEntryNode list-a = {",
            "        leafNode leaf-a = \"foo\"",
            "    }",
            "}"), createMapNode().prettyTree().get());
    }

    @Test
    public void testMapEntryPrettyTree() {
        assertEquals(String.join("\n",
            "mapEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)list-a = {",
            "    leafNode leaf-a = \"bar\"",
            "    systemMapNode list-b = {",
            "        mapEntryNode list-b = {",
            "            leafNode leaf-b = \"two\"",
            "        }",
            "        mapEntryNode list-b = {",
            "            leafNode leaf-b = \"one\"",
            "        }",
            "    }",
            "}"), createMapEntryNode().prettyTree().get());
    }

    @Test
    public void testChoicePrettyTree() {
        assertEquals(String.join("\n",
            "choiceNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)choice = {",
            "    leafNode augment = \"Augmented leaf value\"",
            "}"), createChoiceNode().prettyTree().get());
    }

    @Test
    public void testLeafPrettyTree() {
        assertEquals("leafNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)leaf = \"Leaf value\"",
            createLeafNode().prettyTree().get());
    }

    @Test
    public void testLeafSetPrettyTree() {
        assertEquals(String.join("\n",
            "systemLeafSetNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)leaf-set = {",
            "    leafSetEntryNode leaf-set = \"Leaf set value\"",
            "}"), createLeafSetNode().prettyTree().get());
    }

    @Test
    public void testUserLeafSetPrettyTree() {
        assertEquals(String.join("\n",
            "userLeafSetNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-leaf-set = {",
            "    leafSetEntryNode user-leaf-set = \"User leaf set value\"",
            "}"), createUserLeafSetNode().prettyTree().get());
    }

    @Test
    public void testUserMapPrettyTree() {
        assertEquals(String.join("\n",
            "userMapNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-map = {",
            "    mapEntryNode user-map = {",
            "        leafNode user-map-entry = \"User map entry value\"",
            "    }",
            "}"), createUserMapNode().prettyTree().get());
    }

    @Test
    public void testUserMapEntryPrettyTree() {
        assertEquals(String.join("\n",
            "mapEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-map = {",
            "    leafNode user-map-entry = \"User map entry value\"",
            "}"), createUserMapEntryNode().prettyTree().get());
    }

    @Test
    public void testUnkeyedListPrettyTree() {
        assertEquals(String.join("\n",
            "unkeyedListNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)unkeyed-list = {",
            "    unkeyedListEntryNode unkeyed-list-entry = {",
            "        leafNode unkeyed-list-leaf = \"Unkeyed list leaf value\"",
            "    }",
            "}"), createUnkeyedListNode().prettyTree().get());
    }

    @Test
    public void testUnkeyedListEntryPrettyTree() {
        assertEquals(String.join("\n",
            "unkeyedListEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)unkeyed-list-entry = {",
            "    leafNode unkeyed-list-leaf = \"Unkeyed list leaf value\"",
            "}"), createUnkeyedListEntryNode().prettyTree().get());
    }

    @Test
    public void testAnyDataPrettyTree() {
        assertEquals(String.join("\n",
            "anydataNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)any-data = (java.lang.String)"),
            createAnyDataNode().prettyTree().get());
    }

    @Test
    public void testContainerPrettyTree() {
        assertEquals(String.join("\n",
            "containerNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)root = {",
            "    userMapNode user-map = {",
            "        mapEntryNode user-map = {",
            "            leafNode user-map-entry = \"User map entry value\"",
            "        }",
            "    }",
            "    userLeafSetNode user-leaf-set = {",
            "        leafSetEntryNode user-leaf-set = \"User leaf set value\"",
            "    }",
            "    systemMapNode list-a = {",
            "        mapEntryNode list-a = {",
            "            leafNode leaf-a = \"bar\"",
            "            systemMapNode list-b = {",
            "                mapEntryNode list-b = {",
            "                    leafNode leaf-b = \"two\"",
            "                }",
            "                mapEntryNode list-b = {",
            "                    leafNode leaf-b = \"one\"",
            "                }",
            "            }",
            "        }",
            "        mapEntryNode list-a = {",
            "            leafNode leaf-a = \"foo\"",
            "        }",
            "    }",
            "    containerNode (urn:opendaylight:controller:sal:dom:store:another)another = {",
            "        systemMapNode list-from-another-namespace = {",
            "            mapEntryNode list-from-another-namespace = {",
            "                leafNode leaf-from-another-namespace = \"Leaf from another namespace value\"",
            "            }",
            "        }",
            "    }",
            "    choiceNode choice = {",
            "        leafNode augment = \"Augmented leaf value\"",
            "    }",
            "    anydataNode any-data = (java.lang.String)",
            "    unkeyedListNode unkeyed-list = {",
            "        unkeyedListEntryNode unkeyed-list-entry = {",
            "            leafNode unkeyed-list-leaf = \"Unkeyed list leaf value\"",
            "        }",
            "    }",
            "    leafNode leaf = \"Leaf value\"",
            "    systemLeafSetNode leaf-set = {",
            "        leafSetEntryNode leaf-set = \"Leaf set value\"",
            "    }",
            "}"), createContainerNode().prettyTree().get());
    }
}
