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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class ExtendedTypedefTest {
    @Test
    void constantGenerationTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            ExtendedTypedefTest.class, "/typedef_of_typedef.yang", "/ietf-models/ietf-inet-types.yang"));

        ScalarTypeObjectArchetype simpleTypedef4 = null;
        UnionTypeObjectArchetype extendedTypedefUnion = null;
        UnionTypeObjectArchetype unionTypedef = null;
        ScalarTypeObjectArchetype typedefFromImport = null;
        for (var type : genTypes) {
            if (type instanceof GeneratedTransferObject gto) {
                if (type.simpleName().equals("SimpleTypedef4")) {
                    simpleTypedef4 = assertInstanceOf(ScalarTypeObjectArchetype.class, gto);
                } else if (type.simpleName().equals("ExtendedTypedefUnion")) {
                    extendedTypedefUnion = assertInstanceOf(UnionTypeObjectArchetype.class, gto);
                } else if (type.simpleName().equals("UnionTypedef")) {
                    unionTypedef = assertInstanceOf(UnionTypeObjectArchetype.class, gto);
                } else if (type.simpleName().equals("TypedefFromImport")) {
                    typedefFromImport = assertInstanceOf(ScalarTypeObjectArchetype.class, gto);
                }
            }
        }

        // typedef-from-import
        assertNotNull(typedefFromImport, "TypedefFromImport not found");
        var extendTO = typedefFromImport.getSuperType();
        assertNotNull(extendTO);
        assertEquals("Ipv4Address", extendTO.simpleName());

        // simple-typedef4
        assertNotNull(simpleTypedef4, "SimpleTypedef4 not found");
        assertNotNull(extendedTypedefUnion, "ExtendedTypedefUnion not found");
        assertNotNull(unionTypedef, "UnionTypedef");

        extendTO = simpleTypedef4.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef4 should have extend.");
        assertEquals("SimpleTypedef3", extendTO.simpleName(), "Incorrect extension for SimpleTypedef4.");

        extendTO = extendTO.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef3 should have extend.");
        assertEquals("SimpleTypedef2", extendTO.simpleName(), "Incorrect extension for SimpleTypedef3.");

        extendTO = extendTO.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef2 should have extend.");
        assertEquals("SimpleTypedef1", extendTO.simpleName(), "SimpleTypedef2 should be extended with SimpleTypedef1.");
        assertEquals(BaseYangTypes.UINT8_TYPE, assertInstanceOf(ScalarTypeObjectArchetype.class, extendTO).valueType());

        assertNull(extendTO.getSuperType(), "SimpleTypedef1 shouldn't have extend.");

        // extended-typedef-union
        assertNotNull(extendedTypedefUnion, "ExtendedTypedefUnion object not found");
        assertEquals(List.of(), extendedTypedefUnion.getProperties(),
            "ExtendedTypedefUnion shouldn't have any property");

        final var extendUTO = extendedTypedefUnion.getSuperType();
        assertNotNull(extendUTO);
        assertEquals("UnionTypedef", extendUTO.simpleName(), "Incorrect extension fo ExtendedTypedefUnion.");
        assertNull(extendUTO.getSuperType(), "UnionTypedef shouldn't be extended");

        assertEquals(List.of("simpleTypedef1", "simpleTypedef4", "byteType", "typedefEnumFruit"),
            extendUTO.typePropertyNames());

        assertEquals(List.of(), extendUTO.enclosedTypes());
        final var utoTypes = extendUTO.typePropertyTypes();
        assertEquals(4, utoTypes.size());

        final var uto1 = assertInstanceOf(ScalarTypeObjectArchetype.class, utoTypes.get(0));
        assertEquals("SimpleTypedef1", uto1.simpleName());
        final var uto2 = assertInstanceOf(ScalarTypeObjectArchetype.class, utoTypes.get(1));
        assertEquals("SimpleTypedef4", uto2.simpleName());
        final var uto3 = assertInstanceOf(BitsTypeObjectArchetype.class, utoTypes.get(2));
        assertEquals("ByteType", uto3.simpleName());
        final var uto4 = assertInstanceOf(EnumTypeObjectArchetype.class, utoTypes.get(3));
        assertEquals("TypedefEnumFruit", uto4.simpleName());
    }
}
