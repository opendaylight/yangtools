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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class BindingGeneratorImplTest {
    @Test
    void isisTotpologyStatementParserTest()  {
        final var generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            BindingGeneratorImplTest.class,
            "/isis-topology/network-topology@2013-10-21.yang", "/isis-topology/isis-topology@2013-10-21.yang",
            "/isis-topology/l3-unicast-igp-topology@2013-10-21.yang"));
        assertEquals(11, generateTypes.size());
    }

    @Test
    void choiceNodeGenerationTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/binding-generator-impl-test/choice-test.yang"));

        GeneratedType choiceTestData = null;
        GeneratedType myRootContainer = null;
        GeneratedType myList = null;
        GeneratedType myContainer = null;
        GeneratedType myList2 = null;
        GeneratedType myContainer2 = null;

        for (var type : generateTypes) {
            switch (type.getName()) {
                case "ChoiceTestData" -> choiceTestData = type;
                case "Myrootcontainer" -> myRootContainer = type;
                case "Mylist" -> myList = type;
                case "Mylist2" -> myList2 = type;
                case "Mycontainer" -> myContainer = type;
                case "Mycontainer2" -> myContainer2 = type;
                default -> {
                    // ignore
                }
            }
        }

        assertNotNull(choiceTestData);
        assertNotNull(myRootContainer);
        assertNotNull(myList);
        assertNotNull(myContainer);
        assertNotNull(myList2);
        assertNotNull(myContainer2);

        Type childOfParamType = null;
        for (var type : myContainer.getImplements()) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = assertInstanceOf(ParameterizedType.class, type).getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertEquals("ChoiceTestData", childOfParamType.getName());

        childOfParamType = null;
        for (var type : myList.getImplements()) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = assertInstanceOf(ParameterizedType.class, type).getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertEquals("ChoiceTestData", childOfParamType.getName());

        childOfParamType = null;
        for (var type : myContainer2.getImplements()) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = assertInstanceOf(ParameterizedType.class, type).getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertEquals("Myrootcontainer", childOfParamType.getName());

        childOfParamType = null;
        for (Type type : myList2.getImplements()) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type).getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertEquals("Myrootcontainer", childOfParamType.getName());
    }

    @Test
    void notificationGenerationTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/binding-generator-impl-test/notification-test.yang"));

        GeneratedType foo = null;
        for (var type : generateTypes) {
            if (type.getName().equals("Foo")) {
                foo = type;
                break;
            }
        }

        assertNotNull(foo);

        Type childOf = null;
        Type dataObject = null;
        for (var type :  foo.getImplements()) {
            switch (type.getName()) {
                case "ChildOf":
                    childOf = type;
                    break;
                case "DataObject":
                    dataObject = type;
                    break;
                default:
                    // ignore
            }
        }

        assertNull(childOf);
        assertNotNull(dataObject);
    }

    @Test
    void testBaseYangTypes() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/base-yang-types.yang"));
        assertEquals(19, types.size());
    }
}
