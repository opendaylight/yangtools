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
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
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
            if (type.simpleName().equals("Interface")) {
                genInterface = type;
            }
        }
        assertNotNull(genInterface, "Generated Type Interface is not present in list of Generated Types");

        EnumTypeObjectArchetype linkUpDownTrapEnable = null;
        EnumTypeObjectArchetype operStatus = null;
        final var enums = genInterface.getEnumerations();
        assertNotNull(enums, "Generated Type Interface cannot contain NULL reference to Enumeration types!");
        assertEquals(2, enums.size(), "Generated Type Interface MUST contain 2 Enumeration Types");
        for (var e : enums) {
            if (e.simpleName().equals("LinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = e;
            } else if (e.simpleName().equals("OperStatus")) {
                operStatus = e;
            }
        }

        assertNotNull(linkUpDownTrapEnable, "Expected Enum LinkUpDownTrapEnable, but was NULL!");
        assertNotNull(operStatus, "Expected Enum OperStatus, but was NULL!");

        assertEquals(2, linkUpDownTrapEnable.valueToConstant().size(),
            "Enum LinkUpDownTrapEnable MUST contain 2 values!");
        assertEquals(7, operStatus.valueToConstant().size(), "Enum OperStatus MUST contain 7 values!");

        final var methods = genInterface.getMethodDefinitions();

        assertNotNull(methods, "Generated Interface cannot contain NULL reference for Method Signature Definitions!");

        // FIXME: split this into getter/default/static asserts
        assertEquals(29, methods.size());
        EnumTypeObjectArchetype ianaIfType = null;
        for (var method : methods) {
            if (method.getName().equals("getType")) {
                if (method.getReturnType() instanceof EnumTypeObjectArchetype enumeration) {
                    ianaIfType = enumeration;
                }
            }
        }

        assertNotNull(ianaIfType, "Method getType MUST return Enumeration Type not NULL reference!");
        assertEquals(272, ianaIfType.valueToConstant().size(), "Enumeration getType MUST contain 272 values!");
    }

    @Test
    void testTypedefEnumResolving() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/ietf-models/iana-if-type.yang"));
        assertNotNull(genTypes);
        assertEquals(2, genTypes.size());

        final var type = assertInstanceOf(EnumTypeObjectArchetype.class, genTypes.get(1));
        assertEquals(272, type.valueToConstant().size(), "Enumeration type MUST contain 272 values!");
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
            if (type.simpleName().equals("Interface") && type.packageName().equals(
                "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces")) {
                genInterface = type;
            }
        }
        assertNotNull(genInterface, "Generated Type Interface is not present in list of Generated Types");

        EnumTypeObjectArchetype linkUpDownTrapEnable = null;
        EnumTypeObjectArchetype operStatus = null;
        final var methods = genInterface.getMethodDefinitions();
        assertNotNull(methods, "Generated Type Interface cannot contain NULL reference to Enumeration types!");

        // FIXME: split this into getter/default/static asserts
        assertEquals(9, methods.size());
        for (var method : methods) {
            if (method.getName().equals("getLinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = assertInstanceOf(EnumTypeObjectArchetype.class, method.getReturnType());
            } else if (method.getName().equals("getOperStatus")) {
                operStatus = assertInstanceOf(EnumTypeObjectArchetype.class, method.getReturnType());
            }
        }

        assertNotNull(linkUpDownTrapEnable, "Expected Referenced Enum LinkUpDownTrapEnable, but was NULL!");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            linkUpDownTrapEnable.name().immediatelyEnclosingClass().toString());

        assertNotNull(operStatus, "Expected Referenced Enum OperStatus, but was NULL!");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface",
            operStatus.name().immediatelyEnclosingClass().toString());
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
        final var dollarContaining = testEnums.getFirst().valueToConstant();
        assertEquals(5, dollarContaining.size());
        final var dcIt = dollarContaining.values().iterator();
        assertEquals("$", dcIt.next());
        assertEquals("$abc", dcIt.next());
        assertEquals("A$bc", dcIt.next());
        assertEquals("Ab$c", dcIt.next());
        assertEquals("Abc$", dcIt.next());
        final var prefixRequired = testEnums.get(1).valueToConstant();
        assertEquals(2, prefixRequired.size());
        final var prIt = prefixRequired.values().iterator();
        assertEquals("_09", prIt.next());
        assertEquals("_1337LeetPro", prIt.next());
        final var invalidIdentifier = testEnums.get(2).valueToConstant();
        assertEquals(1, invalidIdentifier.size());
        assertEquals("$_", invalidIdentifier.values().iterator().next());
        final var invalidChars = testEnums.get(3).valueToConstant();
        assertEquals(5, invalidChars.size());
        final var icIt = invalidChars.values().iterator();
        assertEquals("$$2A$", icIt.next());
        assertEquals("$$2E$", icIt.next());
        assertEquals("$$2F$", icIt.next());
        assertEquals("$$3F$", icIt.next());
        assertEquals("$a$2A$a", icIt.next());

        // ------------------- container okay-identifier -----------------------
        final var okayIdentifier = genTypes.get(2).getEnumerations();
        assertEquals(2, okayIdentifier.size());
        final var underscores = okayIdentifier.getFirst().valueToConstant();
        assertEquals(1, underscores.size());
        assertEquals("__", underscores.values().iterator().next());
        final var wordsCapitalCamelCase = okayIdentifier.getLast().valueToConstant();
        assertEquals(2, wordsCapitalCamelCase.size());
        final var wcccIt = wordsCapitalCamelCase.values().iterator();
        assertEquals("True", wcccIt.next());
        assertEquals("ĽaľahoPapľuhu", wcccIt.next());

        // ------------------- container conflicting-names -----------------------
        final var conflictingNames = genTypes.get(3).getEnumerations();
        assertEquals(4, conflictingNames.size());
        final var conflict1 = conflictingNames.get(0).valueToConstant();
        assertEquals(3, conflict1.size());
        final var c1it = conflict1.values().iterator();
        assertEquals("_09", c1it.next());
        assertEquals("$09", c1it.next());
        assertEquals("$0$2D$9", c1it.next());
        final var conflict2 = conflictingNames.get(1).valueToConstant();
        assertEquals(2, conflict2.size());
        final var c2it = conflict2.values().iterator();
        assertEquals("aZ", c2it.next());
        assertEquals("$a$2D$z", c2it.next());
        final var conflict3 = conflictingNames.get(2).valueToConstant();
        assertEquals(3, conflict3.size());
        final var c3it = conflict3.values().iterator();
        assertEquals("$a2$2E$5", c3it.next());
        assertEquals("a25", c3it.next());
        assertEquals("$a2$2D$5", c3it.next());
        final var conflict4 = conflictingNames.get(3).valueToConstant();
        assertEquals(2, conflict4.size());
        final var c4it = conflict4.values().iterator();
        assertEquals("$ľaľaho$20$papľuhu", c4it.next());
        assertEquals("$ľaľaho$20$$20$papľuhu", c4it.next());
    }
}
