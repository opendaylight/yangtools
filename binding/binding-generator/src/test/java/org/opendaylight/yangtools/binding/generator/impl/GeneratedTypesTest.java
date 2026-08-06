/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
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

        // FIXME: pick one of the accesses
        var simpleContainer = assertInstanceOf(ContainerObjectArchetype.class, genTypes.get(1));
        var nestedContainer = assertInstanceOf(ContainerObjectArchetype.class, genTypes.get(2));
        for (var t : genTypes) {
            if ("SimpleContainer".equals(t.simpleName())) {
                simpleContainer = assertInstanceOf(ContainerObjectArchetype.class, t);
            } else if ("NestedContainer".equals(t.simpleName())) {
                nestedContainer = assertInstanceOf(ContainerObjectArchetype.class, t);
            }
        }
        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        assertEquals(3, simpleContainer.getMethodDefinitions().size());
        assertEquals(2, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (var method : simpleContainer.getMethodDefinitions()) {
            switch (method.name()) {
                case "getFoo" -> {
                    getFooMethodCounter++;
                    getFooMethodReturnTypeName = method.returnType().simpleName();
                }
                case "getBar" -> {
                    getBarMethodCounter++;
                    getBarMethodReturnTypeName = method.returnType().simpleName();
                }
                case "getNestedContainer" -> {
                    getNestedContainerCounter++;
                    getNestedContainerReturnTypeName = method.returnType().simpleName();
                }
                default -> {
                    // no-op
                }
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
            switch (method.name()) {
                case "getFoo" -> {
                    getFooMethodCounter++;
                    getFooMethodReturnTypeName = method.returnType().simpleName();
                }
                case "getBar" -> {
                    getBarMethodCounter++;
                    getBarMethodReturnTypeName = method.returnType().simpleName();
                }
                default -> {
                    // no-op
                }
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

        // FIXME: pick one of the accesses...
        var simpleContainer = assertInstanceOf(ContainerObjectArchetype.class, genTypes.get(1));
        var nestedContainer = assertInstanceOf(ContainerObjectArchetype.class, genTypes.get(2));
        for (var t : genTypes) {
            if ("SimpleContainer".equals(t.simpleName())) {
                simpleContainer = assertInstanceOf(ContainerObjectArchetype.class, t);
            } else if ("NestedContainer".equals(t.simpleName())) {
                nestedContainer = assertInstanceOf(ContainerObjectArchetype.class, t);
            }
        }

        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        assertEquals(3, simpleContainer.getMethodDefinitions().size());
        assertEquals(2, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (var method : simpleContainer.getMethodDefinitions()) {
            switch (method.name()) {
                case "getFoo" -> {
                    getFooMethodCounter++;
                    assertEquals(BaseYangTypes.INT32_TYPE, method.returnType());
                    getFooMethodReturnTypeName = method.javaReturnType().simpleName();
                }
                case "getBar" -> {
                    getBarMethodCounter++;
                    assertEquals(BaseYangTypes.STRING_TYPE, method.returnType());
                    getBarMethodReturnTypeName = method.javaReturnType().simpleName();
                }
                case "getNestedContainer" -> {
                    getNestedContainerCounter++;
                    assertInstanceOf(ContainerObjectArchetype.class, method.returnType());
                    getNestedContainerReturnTypeName = method.javaReturnType().simpleName();
                }
                default -> {
                    // no-op
                }
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
            switch (method.name()) {
                case "getFoo" -> {
                    getFooMethodCounter++;
                    assertEquals(BaseYangTypes.UINT8_TYPE, method.returnType());
                    getFooMethodReturnTypeName = method.javaReturnType().simpleName();
                }
                case "getBar" -> {
                    getBarMethodCounter++;
                    assertEquals(BaseYangTypes.STRING_TYPE, method.returnType());
                    getBarMethodReturnTypeName = method.javaReturnType().simpleName();
                }
                default -> {
                    // no-op
                }
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

        int getListChildContainerMethodCount = 0;
        int getFooMethodCount = 0;
        int setFooMethodCount = 0;
        int getSimpleLeafListMethodCount = 0;
        int setSimpleLeafListMethodCount = 0;
        int getBarMethodCount = 0;

        String getListChildContainerMethodReturnTypeName = "";

        for (var genType : genTypes) {
            switch (genType) {
                case KeyArchetype key -> {
                    final var methods = key.methods();

                    assertEquals(0, listKeyClassCount++, "Unexpected key");
                    assertEquals(1, methods.size());

                    final var first = methods.entrySet().iterator().next();
                    assertEquals("list-key", first.getKey());
                    assertEquals("Byte", first.getValue().returnType().simpleName());
                }
                case ContainerObjectArchetype archetype -> {
                    switch (archetype.simpleName()) {
                        case "ListParentContainer" ->
                            listParentContainerMethodsCount = archetype.getMethodDefinitions().size();
                        case "ListChildContainer" ->
                            listChildContainerMethodsCount = archetype.getMethodDefinitions().size();
                        default -> {
                            // no-op
                        }
                    }
                }
                case EntryObjectArchetype archetype -> {
                    if (archetype.simpleName().equals("SimpleList")) {
                        assertEquals(JavaTypeName.create(
                            "org.opendaylight.yang.gen.v1.urn.simple.container.demo.rev130227.list.parent.container",
                            "SimpleListKey"), archetype.keyName());

                        simpleListMethodsCount = archetype.getMethodDefinitions().size();
                        for (var method : archetype.getMethodDefinitions()) {
                            switch (method.name()) {
                                case "getListChildContainer":
                                    getListChildContainerMethodCount++;
                                    getListChildContainerMethodReturnTypeName = method.returnType().simpleName();
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
                    }
                }
                default -> {
                    // no-op
                }
            }
        }

        assertEquals(1, listParentContainerMethodsCount);
        assertEquals(1, listChildContainerMethodsCount);
        assertEquals(1, listKeyClassCount);

        assertEquals(1, getListChildContainerMethodCount);
        assertEquals("ListChildContainer", getListChildContainerMethodReturnTypeName);
        assertEquals(1, getFooMethodCount);
        assertEquals(0, setFooMethodCount);
        assertEquals(1, getSimpleLeafListMethodCount);
        assertEquals(0, setSimpleLeafListMethodCount);
        assertEquals(1, getBarMethodCount);

        assertEquals(5, simpleListMethodsCount);
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
            if (!(type instanceof KeyArchetype key)) {
                genTypesCount++;
            } else if (key.simpleName().equals("CompositeKeyListKey")) {
                compositeKeyListKeyCount++;
                assertEquals(Set.of("key1", "key2"), key.methods().keySet());
                compositeKeyListKeyPropertyCount += 2;
                genTOsCount++;
            } else if (key.simpleName().equals("InnerListKey")) {
                innerListKeyPropertyCount = key.methods().size();
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
        int genKeysCount = 0;
        for (var type : genTypes) {
            if (type instanceof KeyArchetype) {
                genKeysCount++;
            } else {
                genTypesCount++;
            }
        }

        assertEquals(11, genTypesCount);
        assertEquals(3, genKeysCount);
    }

    @Test
    void testAugmentRpcInput() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/augment-rpc-input.yang"));
        assertEquals(6, genTypes.size());
    }
}
