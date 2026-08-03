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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesBitsTest {
    @Test
    void testGeneretedTypesBitsTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-bits-demo.yang"));
        assertNotNull(genTypes);

        List<GetterMethod> gettersList = null;

        boolean leafParentFound = false;

        boolean byteTypeFound = false;
        int classPropertiesNumb = 0;

        boolean getByteLeafMethodFound = false;

        for (var genType : genTypes) {
            if (genType instanceof TypeObjectArchetype<?> archetype) {
                if (archetype.simpleName().equals("ByteType")) {
                    byteTypeFound = true;
                    final var bits = assertInstanceOf(BitsTypeObjectArchetype.class, archetype);
                    assertNull(bits.superType());
                    final var def = bits.typeDefinition();
                    assertEquals(QName.create("urn:simple:bits:demo", "2013-06-11", "byte-type"), def.getQName());
                    final var base = def.getBaseType();
                    assertNotNull(base);
                    assertEquals(QName.create("urn:simple:bits:demo", "2013-06-11", "bits"), base.getQName());
                    classPropertiesNumb = def.getBits().size();
                }
            } else if (genType.simpleName().equals("LeafParentContainer")) {
                leafParentFound = true;
                // check of methods
                gettersList = assertInstanceOf(ContainerObjectArchetype.class, genType).getters();
                if (gettersList != null) {
                    // loop through all methods
                    for (var methodSignature : gettersList) {
                        switch (methodSignature.suffix()) {
                            case "ByteLeaf" -> {
                                getByteLeafMethodFound = true;
                                assertEquals("ByteType", methodSignature.returnType().simpleName());
                            }
                            default -> {
                                // no-op
                            }
                        }
                    }
                }
            }
        }

        assertTrue(byteTypeFound);

        assertEquals(8, classPropertiesNumb);

        assertTrue(leafParentFound);

        assertNotNull(gettersList);

        assertTrue(getByteLeafMethodFound);
    }
}
