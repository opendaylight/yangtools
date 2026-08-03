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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationArchetype;
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

        DataRootArchetype choiceTestData = null;
        ContainerObjectArchetype myRootContainer = null;
        ItemObjectArchetype myList = null;
        ContainerObjectArchetype myContainer = null;
        ItemObjectArchetype myList2 = null;
        ContainerObjectArchetype myContainer2 = null;

        for (var type : generateTypes) {
            switch (type.simpleName()) {
                case "ChoiceTestData" -> choiceTestData = assertInstanceOf(DataRootArchetype.class, type);
                case "Myrootcontainer" -> myRootContainer = assertInstanceOf(ContainerObjectArchetype.class, type);
                case "Mylist" -> myList = assertInstanceOf(ItemObjectArchetype.class, type);
                case "Mylist2" -> myList2 = assertInstanceOf(ItemObjectArchetype.class, type);
                case "Mycontainer" -> myContainer = assertInstanceOf(ContainerObjectArchetype.class, type);
                case "Mycontainer2" -> myContainer2 = assertInstanceOf(ContainerObjectArchetype.class, type);
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

        assertEquals("ChoiceTestData", myContainer.parentName().simpleName());
        assertEquals("ChoiceTestData", myList.parentName().simpleName());
        assertEquals("Myrootcontainer", myContainer2.parentName().simpleName());
        assertEquals("Myrootcontainer", myList2.parentName().simpleName());
    }

    @Test
    void notificationGenerationTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/binding-generator-impl-test/notification-test.yang"));

        NotificationArchetype foo = null;
        for (var type : generateTypes) {
            if (type.simpleName().equals("Foo")) {
                foo = assertInstanceOf(NotificationArchetype.class, type);
                break;
            }
        }

        assertNotNull(foo);
    }

    @Test
    void testBaseYangTypes() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/base-yang-types.yang"));
        assertEquals(19, types.size());
    }
}
