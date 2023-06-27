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
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GenEnumResolvingTest {
    @Test
    void testLeafEnumResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            GenEnumResolvingTest.class,
            "/enum-test-models/ietf-interfaces@2012-11-15.yang", "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);

        assertEquals(9, genTypes.size());

        GeneratedType genInterface = null;
        for (var type : genTypes) {
            if (type.getName().equals("Interface")) {
                genInterface = type;
            }
        }
        assertNotNull(genInterface, "Generated Type Interface is not present in list of Generated Types");

        Enumeration linkUpDownTrapEnable = null;
        Enumeration operStatus = null;
        final var enums = genInterface.getEnumerations();
        assertNotNull(enums, "Generated Type Interface cannot contain NULL reference to Enumeration types!");
        assertEquals(2, enums.size(), "Generated Type Interface MUST contain 2 Enumeration Types");
        for (var e : enums) {
            if (e.getName().equals("LinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = e;
            } else if (e.getName().equals("OperStatus")) {
                operStatus = e;
            }
        }

        assertNotNull(linkUpDownTrapEnable, "Expected Enum LinkUpDownTrapEnable, but was NULL!");
        assertNotNull(operStatus, "Expected Enum OperStatus, but was NULL!");

        assertNotNull(linkUpDownTrapEnable.getValues(),
            "Enum LinkUpDownTrapEnable MUST contain Values definition not NULL reference!");
        assertNotNull(operStatus.getValues(), "Enum OperStatus MUST contain Values definition not NULL reference!");
        assertEquals(2, linkUpDownTrapEnable.getValues().size(), "Enum LinkUpDownTrapEnable MUST contain 2 values!");
        assertEquals(7, operStatus.getValues().size(), "Enum OperStatus MUST contain 7 values!");

        final var methods = genInterface.getMethodDefinitions();

        assertNotNull(methods, "Generated Interface cannot contain NULL reference for Method Signature Definitions!");

        // FIXME: split this into getter/default/static asserts
        assertEquals(33, methods.size());
        Enumeration ianaIfType = null;
        for (var method : methods) {
            if (method.getName().equals("getType")) {
                if (method.getReturnType() instanceof Enumeration enumeration) {
                    ianaIfType = enumeration;
                }
            }
        }

        assertNotNull(ianaIfType, "Method getType MUST return Enumeration Type not NULL reference!");
        assertEquals(272, ianaIfType.getValues().size(), "Enumeration getType MUST contain 272 values!");
    }

    @Test
    void testTypedefEnumResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);
        assertEquals(2, genTypes.size());

        final var type = assertInstanceOf(Enumeration.class, genTypes.get(1));
        assertEquals(272, type.getValues().size(), "Enumeration type MUST contain 272 values!");
    }

    @Test
    void testLeafrefEnumResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            GenEnumResolvingTest.class,
            "/enum-test-models/abstract-topology@2013-02-08.yang", "/enum-test-models/ietf-interfaces@2012-11-15.yang",
            "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);
        assertEquals(27, genTypes.size());

        GeneratedType genInterface = null;
        for (var type : genTypes) {
            if (type.getName().equals("Interface") && type.getPackageName().equals(
                "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces")) {
                genInterface = type;
            }
        }
        assertNotNull(genInterface, "Generated Type Interface is not present in list of Generated Types");

        Enumeration linkUpDownTrapEnable = null;
        Enumeration operStatus = null;
        final var methods = genInterface.getMethodDefinitions();
        assertNotNull(methods, "Generated Type Interface cannot contain NULL reference to Enumeration types!");

        // FIXME: split this into getter/default/static asserts
        assertEquals(13, methods.size());
        for (var method : methods) {
            if (method.getName().equals("getLinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = assertInstanceOf(Enumeration.class, method.getReturnType());
            } else if (method.getName().equals("getOperStatus")) {
                operStatus = assertInstanceOf(Enumeration.class, method.getReturnType());
            }
        }

        assertNotNull(linkUpDownTrapEnable, "Expected Referenced Enum LinkUpDownTrapEnable, but was NULL!");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            linkUpDownTrapEnable.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());

        assertNotNull(operStatus, "Expected Referenced Enum OperStatus, but was NULL!");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            operStatus.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());
    }

    @Test
    public void testEnumNamesMapping() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/enum-test-models/enum-names-mapping@2023-02-17.yang"));
        assertNotNull(genTypes);
        assertEquals(4, genTypes.size());

        // ------------------- container test-enums -----------------------
        final var testEnums = genTypes.get(1).getEnumerations();
        assertEquals(4, testEnums.size());
        final var dollarContaining = testEnums.get(0).getValues();
        assertEquals(5, dollarContaining.size());
        assertEquals("$", dollarContaining.get(0).getMappedName());
        assertEquals("$abc", dollarContaining.get(1).getMappedName());
        assertEquals("A$bc", dollarContaining.get(2).getMappedName());
        assertEquals("Ab$c", dollarContaining.get(3).getMappedName());
        assertEquals("Abc$", dollarContaining.get(4).getMappedName());
        final var prefixRequired = testEnums.get(1).getValues();
        assertEquals(2, prefixRequired.size());
        assertEquals("_09", prefixRequired.get(0).getMappedName());
        assertEquals("_1337LeetPro", prefixRequired.get(1).getMappedName());
        final var invalidIdentifier = testEnums.get(2).getValues();
        assertEquals(1, invalidIdentifier.size());
        assertEquals("$_", invalidIdentifier.get(0).getMappedName());
        final var invalidChars = testEnums.get(3).getValues();
        assertEquals(5, invalidChars.size());
        assertEquals("$$2A$", invalidChars.get(0).getMappedName());
        assertEquals("$$2E$", invalidChars.get(1).getMappedName());
        assertEquals("$$2F$", invalidChars.get(2).getMappedName());
        assertEquals("$$3F$", invalidChars.get(3).getMappedName());
        assertEquals("$a$2A$a", invalidChars.get(4).getMappedName());

        // ------------------- container okay-identifier -----------------------
        final var okayIdentifier = genTypes.get(2).getEnumerations();
        assertEquals(2, okayIdentifier.size());
        final var underscores = okayIdentifier.get(0).getValues();
        assertEquals(1, underscores.size());
        assertEquals("__", underscores.get(0).getMappedName());
        final var wordsCapitalCamelCase = okayIdentifier.get(1).getValues();
        assertEquals(2, wordsCapitalCamelCase.size());
        assertEquals("True", wordsCapitalCamelCase.get(0).getMappedName());
        assertEquals("ĽaľahoPapľuhu", wordsCapitalCamelCase.get(1).getMappedName());

        // ------------------- container conflicting-names -----------------------
        final var conflictingNames = genTypes.get(3).getEnumerations();
        assertEquals(4, conflictingNames.size());
        final var conflict1 = conflictingNames.get(0).getValues();
        assertEquals(3, conflict1.size());
        assertEquals("_09", conflict1.get(0).getMappedName());
        assertEquals("$09", conflict1.get(1).getMappedName());
        assertEquals("$0$2D$9", conflict1.get(2).getMappedName());
        final var conflict2 = conflictingNames.get(1).getValues();
        assertEquals(2, conflict2.size());
        assertEquals("aZ", conflict2.get(0).getMappedName());
        assertEquals("$a$2D$z", conflict2.get(1).getMappedName());
        final var conflict3 = conflictingNames.get(2).getValues();
        assertEquals(3, conflict3.size());
        assertEquals("$a2$2E$5", conflict3.get(0).getMappedName());
        assertEquals("a25", conflict3.get(1).getMappedName());
        assertEquals("$a2$2D$5", conflict3.get(2).getMappedName());
        final var conflict4 = conflictingNames.get(3).getValues();
        assertEquals(2, conflict4.size());
        assertEquals("$ľaľaho$20$papľuhu", conflict4.get(0).getMappedName());
        assertEquals("$ľaľaho$20$$20$papľuhu", conflict4.get(1).getMappedName());
    }
}
