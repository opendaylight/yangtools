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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class ExtendedTypedefTest {
    @Test
    void constantGenerationTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            ExtendedTypedefTest.class, "/typedef_of_typedef.yang", "/ietf-models/ietf-inet-types.yang"));

        GeneratedTransferObject simpleTypedef4 = null;
        GeneratedTransferObject extendedTypedefUnion = null;
        GeneratedTransferObject unionTypedef = null;
        GeneratedTransferObject typedefFromImport = null;
        for (var type : genTypes) {
            if (type instanceof GeneratedTransferObject gto) {
                if (type.getName().equals("SimpleTypedef4")) {
                    simpleTypedef4 = gto;
                } else if (type.getName().equals("ExtendedTypedefUnion")) {
                    extendedTypedefUnion = gto;
                } else if (type.getName().equals("UnionTypedef")) {
                    unionTypedef = gto;
                } else if (type.getName().equals("TypedefFromImport")) {
                    typedefFromImport = gto;
                }
            }
        }

        // typedef-from-import
        assertNotNull(typedefFromImport, "TypedefFromImport not found");
        assertEquals(List.of(), typedefFromImport.getProperties(), "Properties of TypedefFromImport should be empty");
        assertEquals("Ipv4Address", typedefFromImport.getSuperType().getName(), "TypedefFromImport should be extended");

        // simple-typedef4
        assertNotNull(simpleTypedef4, "SimpleTypedef4 not found");
        assertNotNull(extendedTypedefUnion, "ExtendedTypedefUnion not found");
        assertNotNull(unionTypedef, "UnionTypedef");

        assertEquals(List.of(), simpleTypedef4.getProperties(), "SimpleTypedef4 shouldn't have properties.");

        var extendTO = simpleTypedef4.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef4 should have extend.");
        assertEquals("SimpleTypedef3", extendTO.getName(), "Incorrect extension for SimpleTypedef4.");
        assertEquals(List.of(), extendTO.getProperties(), "SimpleTypedef3 shouldn't have properties.");

        extendTO = extendTO.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef3 should have extend.");
        assertEquals("SimpleTypedef2", extendTO.getName(), "Incorrect extension for SimpleTypedef3.");
        assertEquals(List.of(), extendTO.getProperties(), "SimpleTypedef2 shouldn't have properties.");

        extendTO = extendTO.getSuperType();
        assertNotNull(extendTO, "SimpleTypedef2 should have extend.");
        assertEquals("SimpleTypedef1", extendTO.getName(), "SimpleTypedef2 should be extended with SimpleTypedef1.");
        var properties = extendTO.getProperties();
        assertEquals(1, properties.size(), "Incorrect number of properties in class SimpleTypedef1.");

        assertEquals("value", properties.getFirst().getName(), "Incorrect property's name");
        assertEquals(BaseYangTypes.UINT8_TYPE, properties.get(0).getReturnType(), "Property's incorrect type");

        extendTO = extendTO.getSuperType();
        assertNull(extendTO, "SimpleTypedef1 shouldn't have extend.");

        // extended-typedef-union
        assertNotNull(extendedTypedefUnion, "ExtendedTypedefUnion object not found");
        assertEquals(List.of(), extendedTypedefUnion.getProperties(),
            "ExtendedTypedefUnion shouldn't have any property");

        extendTO = extendedTypedefUnion.getSuperType();
        assertEquals("UnionTypedef", extendTO.getName(), "Incorrect extension fo ExtendedTypedefUnion.");
        assertNull(extendTO.getSuperType(), "UnionTypedef shouldn't be extended");

        properties = extendTO.getProperties();
        assertEquals(4, properties.size(), "Incorrect number of properties for UnionTypedef.");

        GeneratedProperty simpleTypedef4Property = null;
        GeneratedProperty simpleTypedef1Property = null;
        GeneratedProperty byteTypeProperty = null;
        GeneratedProperty typedefEnumFruitProperty = null;
        for (var genProperty : properties) {
            if (genProperty.getName().equals("simpleTypedef1")) {
                simpleTypedef1Property = genProperty;
            } else if (genProperty.getName().equals("simpleTypedef4")) {
                simpleTypedef4Property = genProperty;
            } else if (genProperty.getName().equals("byteType")) {
                byteTypeProperty = genProperty;
            } else if (genProperty.getName().equals("typedefEnumFruit")) {
                typedefEnumFruitProperty = genProperty;
            }
        }

        assertNotNull(simpleTypedef4Property, "simpleTypedef4 property not found in UnionTypedef");
        assertNotNull(simpleTypedef1Property, "simpleTypedef1 property not found in UnionTypedef");
        assertNotNull(byteTypeProperty, "byteType property not found in UnionTypedef");
        assertNotNull(typedefEnumFruitProperty, "typedefEnumFruit property not found in UnionTypedef");

        assertEquals("SimpleTypedef4", simpleTypedef4Property.getReturnType().getName(),
            "Incorrect type for property simpleTypedef4.");
        assertEquals("SimpleTypedef1", simpleTypedef1Property.getReturnType().getName(),
            "Incorrect type for property simpleTypedef1.");
        assertEquals("ByteType", byteTypeProperty.getReturnType().getName(), "Incorrect type for property byteType.");
        assertEquals("TypedefEnumFruit", typedefEnumFruitProperty.getReturnType().getName(),
            "Incorrect type for property typedefEnumFruit.");
    }
}
