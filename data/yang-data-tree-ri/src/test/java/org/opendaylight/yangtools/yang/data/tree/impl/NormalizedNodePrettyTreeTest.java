/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NormalizedNodePrettyTreeTest extends AbstractPrettyTreeTest {
    @Test
    void testMapNodePrettyTree() {
        assertEquals("""
            systemMapNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)list-a = {
                mapEntryNode list-a = {
                    leafNode leaf-a = "bar"
                    systemMapNode list-b = {
                        mapEntryNode list-b = {
                            leafNode leaf-b = "two"
                        }
                        mapEntryNode list-b = {
                            leafNode leaf-b = "one"
                        }
                    }
                }
                mapEntryNode list-a = {
                    leafNode leaf-a = "foo"
                }
            }""", createMapNode().prettyTree().get());
    }

    @Test
    void testMapEntryPrettyTree() {
        assertEquals("""
            mapEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)list-a = {
                leafNode leaf-a = "bar"
                systemMapNode list-b = {
                    mapEntryNode list-b = {
                        leafNode leaf-b = "two"
                    }
                    mapEntryNode list-b = {
                        leafNode leaf-b = "one"
                    }
                }
            }""", createMapEntryNode().prettyTree().get());
    }

    @Test
    void testChoicePrettyTree() {
        assertEquals("""
            choiceNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)choice = {
                leafNode augment = "Augmented leaf value"
            }""", createChoiceNode().prettyTree().get());
    }

    @Test
    void testLeafPrettyTree() {
        assertEquals("leafNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)leaf = \"Leaf value\"",
            createLeafNode().prettyTree().get());
    }

    @Test
    void testLeafSetPrettyTree() {
        assertEquals("""
            systemLeafSetNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)leaf-set = {
                leafSetEntryNode leaf-set = "Leaf set value"
            }""", createLeafSetNode().prettyTree().get());
    }

    @Test
    void testUserLeafSetPrettyTree() {
        assertEquals("""
            userLeafSetNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-leaf-set = {
                leafSetEntryNode user-leaf-set = "User leaf set value"
            }""", createUserLeafSetNode().prettyTree().get());
    }

    @Test
    void testUserMapPrettyTree() {
        assertEquals("""
            userMapNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-map = {
                mapEntryNode user-map = {
                    leafNode user-map-entry = "User map entry value"
                }
            }""", createUserMapNode().prettyTree().get());
    }

    @Test
    void testUserMapEntryPrettyTree() {
        assertEquals("""
            mapEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)user-map = {
                leafNode user-map-entry = "User map entry value"
            }""", createUserMapEntryNode().prettyTree().get());
    }

    @Test
    void testUnkeyedListPrettyTree() {
        assertEquals("""
            unkeyedListNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)unkeyed-list = {
                unkeyedListEntryNode unkeyed-list-entry = {
                    leafNode unkeyed-list-leaf = "Unkeyed list leaf value"
                }
            }""", createUnkeyedListNode().prettyTree().get());
    }

    @Test
    void testUnkeyedListEntryPrettyTree() {
        assertEquals("""
            unkeyedListEntryNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)unkeyed-list-entry = {
                leafNode unkeyed-list-leaf = "Unkeyed list leaf value"
            }""", createUnkeyedListEntryNode().prettyTree().get());
    }

    @Test
    void testAnyDataPrettyTree() {
        assertEquals(
            "anydataNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)any-data = (java.lang.String)",
            createAnyDataNode().prettyTree().get());
    }

    @Test
    void testContainerPrettyTree() {
        assertEquals("""
            containerNode (urn:opendaylight:controller:sal:dom:store:test@2014-03-13)root = {
                userMapNode user-map = {
                    mapEntryNode user-map = {
                        leafNode user-map-entry = "User map entry value"
                    }
                }
                userLeafSetNode user-leaf-set = {
                    leafSetEntryNode user-leaf-set = "User leaf set value"
                }
                systemMapNode list-a = {
                    mapEntryNode list-a = {
                        leafNode leaf-a = "bar"
                        systemMapNode list-b = {
                            mapEntryNode list-b = {
                                leafNode leaf-b = "two"
                            }
                            mapEntryNode list-b = {
                                leafNode leaf-b = "one"
                            }
                        }
                    }
                    mapEntryNode list-a = {
                        leafNode leaf-a = "foo"
                    }
                }
                containerNode (urn:opendaylight:controller:sal:dom:store:another)another = {
                    systemMapNode list-from-another-namespace = {
                        mapEntryNode list-from-another-namespace = {
                            leafNode leaf-from-another-namespace = "Leaf from another namespace value"
                        }
                    }
                }
                choiceNode choice = {
                    leafNode augment = "Augmented leaf value"
                }
                anydataNode any-data = (java.lang.String)
                unkeyedListNode unkeyed-list = {
                    unkeyedListEntryNode unkeyed-list-entry = {
                        leafNode unkeyed-list-leaf = "Unkeyed list leaf value"
                    }
                }
                leafNode leaf = "Leaf value"
                systemLeafSetNode leaf-set = {
                    leafSetEntryNode leaf-set = "Leaf set value"
                }
            }""", createContainerNode().prettyTree().get());
    }
}
