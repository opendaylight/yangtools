/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesTest {
    @Test
    void testMultipleModulesResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResources(GeneratedTypesTest.class,
                "/abstract-topology.yang", "/ietf-models/ietf-inet-types.yang"));
        assertEquals(27, genTypes.size());
    }

    @Test
    void testContainerResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-container-demo.yang"));

        assertNotNull(genTypes);
        assertEquals(3, genTypes.size());

        var simpleContainer = genTypes.get(1);
        var nestedContainer = genTypes.get(2);
        for (var t : genTypes) {
            if ("SimpleContainer".equals(t.getName())) {
                simpleContainer = t;
            } else if ("NestedContainer".equals(t.getName())) {
                nestedContainer = t;
            }
        }
        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        // FIXME: split this into getter/default/static asserts
        assertEquals(10, simpleContainer.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(8, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (var method : simpleContainer.getMethodDefinitions()) {
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getNestedContainer")) {
                getNestedContainerCounter++;
                getNestedContainerReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Integer", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);

        assertEquals(1, getNestedContainerCounter);
        assertEquals("NestedContainer", getNestedContainerReturnTypeName);

        getFooMethodCounter = 0;
        getBarMethodCounter = 0;

        getFooMethodReturnTypeName = "";
        getBarMethodReturnTypeName = "";

        for (var method : nestedContainer.getMethodDefinitions()) {
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Uint8", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);
    }

    @Test
    void testLeafListResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-leaf-list-demo.yang"));

        assertNotNull(genTypes);
        assertEquals(3, genTypes.size());

        var simpleContainer = genTypes.get(1);
        var nestedContainer = genTypes.get(2);
        for (var t : genTypes) {
            if ("SimpleContainer".equals(t.getName())) {
                simpleContainer = t;
            } else if ("NestedContainer".equals(t.getName())) {
                nestedContainer = t;
            }
        }
        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        // FIXME: split this into getter/default/static asserts
        assertEquals(10, simpleContainer.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(8, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (var method : simpleContainer.getMethodDefinitions()) {
            if (method.isDefault()) {
                continue;
            }
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getNestedContainer")) {
                getNestedContainerCounter++;
                getNestedContainerReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Set", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);

        assertEquals(1, getNestedContainerCounter);
        assertEquals("NestedContainer", getNestedContainerReturnTypeName);

        getFooMethodCounter = 0;
        getBarMethodCounter = 0;

        getFooMethodReturnTypeName = "";
        getBarMethodReturnTypeName = "";

        for (var method : nestedContainer.getMethodDefinitions()) {
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Uint8", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("Set", getBarMethodReturnTypeName);
    }

    @Test
    void testListResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-list-demo.yang"));

        assertNotNull(genTypes);
        assertEquals(5, genTypes.size());

        int listParentContainerMethodsCount = 0;
        int simpleListMethodsCount = 0;
        int listChildContainerMethodsCount = 0;
        int listKeyClassCount = 0;

        int getSimpleListKeyMethodCount = 0;
        int getListChildContainerMethodCount = 0;
        int getFooMethodCount = 0;
        int setFooMethodCount = 0;
        int getSimpleLeafListMethodCount = 0;
        int setSimpleLeafListMethodCount = 0;
        int getBarMethodCount = 0;

        String getSimpleListKeyMethodReturnTypeName = "";
        String getListChildContainerMethodReturnTypeName = "";

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject genTO)) {
                if (genType.getName().equals("ListParentContainer")) {
                    listParentContainerMethodsCount = genType.getMethodDefinitions().size();
                } else if (genType.getName().equals("SimpleList")) {
                    simpleListMethodsCount = genType.getMethodDefinitions().size();
                    for (var method : genType.getMethodDefinitions()) {
                        switch (method.getName()) {
                            case Naming.KEY_AWARE_KEY_NAME:
                                getSimpleListKeyMethodCount++;
                                getSimpleListKeyMethodReturnTypeName = method.getReturnType().getName();
                                break;
                            case "getListChildContainer":
                                getListChildContainerMethodCount++;
                                getListChildContainerMethodReturnTypeName = method.getReturnType().getName();
                                break;
                            case "getFoo":
                                getFooMethodCount++;
                                break;
                            case "setFoo":
                                setFooMethodCount++;
                                break;
                            case "getSimpleLeafList":
                                getSimpleLeafListMethodCount++;
                                break;
                            case "setSimpleLeafList":
                                setSimpleLeafListMethodCount++;
                                break;
                            case "getBar":
                                getBarMethodCount++;
                                break;
                            default:
                        }
                    }
                } else if (genType.getName().equals("ListChildContainer")) {
                    listChildContainerMethodsCount = genType.getMethodDefinitions().size();
                }
            } else {
                final var properties = genTO.getProperties();
                final var hashProps = genTO.getHashCodeIdentifiers();
                final var equalProps = genTO.getEqualsIdentifiers();

                assertEquals(0, listKeyClassCount++, "Unexpected key");
                assertEquals(1, properties.size());
                assertEquals("listKey", properties.getFirst().getName());
                assertEquals("Byte", properties.getFirst().getReturnType().getName());
                assertTrue(properties.getFirst().isReadOnly());

                assertEquals(1, hashProps.size());
                assertEquals("listKey", hashProps.getFirst().getName());
                assertEquals("Byte", hashProps.getFirst().getReturnType().getName());

                assertEquals(1, equalProps.size());
                assertEquals("listKey", equalProps.getFirst().getName());
                assertEquals("Byte",  equalProps.getFirst().getReturnType().getName());
            }
        }

        // FIXME: split this into getter/default/static asserts
        assertEquals(6, listParentContainerMethodsCount);
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, listChildContainerMethodsCount);
        assertEquals(1, getSimpleListKeyMethodCount);
        assertEquals(1, listKeyClassCount);

        assertEquals("SimpleListKey", getSimpleListKeyMethodReturnTypeName);

        assertEquals(1, getListChildContainerMethodCount);
        assertEquals("ListChildContainer", getListChildContainerMethodReturnTypeName);
        assertEquals(1, getFooMethodCount);
        assertEquals(0, setFooMethodCount);
        assertEquals(1, getSimpleLeafListMethodCount);
        assertEquals(0, setSimpleLeafListMethodCount);
        assertEquals(1, getBarMethodCount);

        // FIXME: split this into getter/default/static asserts
        assertEquals(15, simpleListMethodsCount);
    }

    @Test
    void testListCompositeKeyResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/list-composite-key.yang"));

        assertNotNull(genTypes);
        assertEquals(7, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;

        int compositeKeyListKeyPropertyCount = 0;
        int compositeKeyListKeyCount = 0;
        int innerListKeyPropertyCount = 0;

        for (var type : genTypes) {
            if (!(type instanceof GeneratedTransferObject genTO)) {
                genTypesCount++;
            } else if (genTO.getName().equals("CompositeKeyListKey")) {
                compositeKeyListKeyCount++;
                for (var prop : genTO.getProperties()) {
                    if (prop.getName().equals("key1") || prop.getName().equals("key2")) {
                        compositeKeyListKeyPropertyCount++;
                    }
                }
                genTOsCount++;
            } else if (genTO.getName().equals("InnerListKey")) {
                innerListKeyPropertyCount =  genTO.getProperties().size();
                genTOsCount++;
            }
        }
        assertEquals(1, compositeKeyListKeyCount);
        assertEquals(2, compositeKeyListKeyPropertyCount);

        assertEquals(1, innerListKeyPropertyCount);

        assertEquals(5, genTypesCount);
        assertEquals(2, genTOsCount);
    }

    @Test
    void testGeneratedTypes() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/demo-topology.yang"));

        assertNotNull(genTypes);
        assertEquals(14, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;
        for (var type : genTypes) {
            if (type instanceof GeneratedTransferObject) {
                genTOsCount++;
            } else {
                genTypesCount++;
            }
        }

        assertEquals(11, genTypesCount);
        assertEquals(3, genTOsCount);
    }

    @Test
    void testAugmentRpcInput() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/augment-rpc-input.yang"));
        assertEquals(6, genTypes.size());
    }
}
