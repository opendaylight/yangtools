/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.ri.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GenerateInnerClassForBitsAndUnionInLeavesTest {
    @Test
    public void testInnerClassCreationForBitsAndUnionsInLeafes() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/bit_and_union_in_leaf.yang"));
        assertEquals(4, genTypes.size());

        boolean parentContainerFound = false;
        boolean bitLeafTOFound = false;
        boolean unionLeafTOFound = false;

        for (GeneratedType type : genTypes) {
            if (!(type instanceof GeneratedTransferObject)) {
                if (type.getName().equals("ParentContainer")) {
                    parentContainerFound = true;
                    GeneratedType parentContainer = type;
                    List<GeneratedType> enclosedTypes = parentContainer.getEnclosedTypes();
                    for (GeneratedType genType : enclosedTypes) {
                        if (genType instanceof GeneratedTransferObject) {
                            final GeneratedTransferObject gto = (GeneratedTransferObject) genType;

                            if (genType.getName().equals("BitLeaf")) {
                                assertFalse("Unexpected duplicate BitLeaf", bitLeafTOFound);
                                bitLeafTOFound = true;

                                List<GeneratedProperty> bitLeafProperties = gto.getProperties();
                                assertEquals(3, bitLeafProperties.size());

                                boolean firstBitPropertyFound = false;
                                boolean secondBitPropertyFound = false;
                                boolean thirdBitPropertyFound = false;
                                for (GeneratedProperty bitLeafProperty : bitLeafProperties) {
                                    if (bitLeafProperty.getName().equals("firstBit")) {
                                        firstBitPropertyFound = true;
                                        assertEquals(Types.primitiveBooleanType(), bitLeafProperty.getReturnType());
                                    } else if (bitLeafProperty.getName().equals("secondBit")) {
                                        secondBitPropertyFound = true;
                                        assertEquals(Types.primitiveBooleanType(), bitLeafProperty.getReturnType());
                                    } else if (bitLeafProperty.getName().equals("thirdBit")) {
                                        thirdBitPropertyFound = true;
                                        assertEquals(Types.primitiveBooleanType(), bitLeafProperty.getReturnType());
                                    }
                                }
                                assertTrue(firstBitPropertyFound);
                                assertTrue(secondBitPropertyFound);
                                assertTrue(thirdBitPropertyFound);
                            } else if (genType.getName().equals("UnionLeaf")) {
                                assertFalse("Unexpected duplicate UnionLeaf", unionLeafTOFound);
                                unionLeafTOFound = true;

                                List<GeneratedProperty> unionLeafProperties = gto.getProperties();
                                assertEquals(3, unionLeafProperties.size());

                                boolean int32UnionPropertyFound = false;
                                boolean stringUnionPropertyFound = false;
                                boolean uint8UnionPropertyFound = false;
                                for (GeneratedProperty unionLeafProperty : unionLeafProperties) {
                                    if (unionLeafProperty.getName().equals("int32")) {
                                        int32UnionPropertyFound = true;
                                        assertEquals(BaseYangTypes.INT32_TYPE, unionLeafProperty.getReturnType());
                                    } else if (unionLeafProperty.getName().equals("string")) {
                                        stringUnionPropertyFound = true;
                                        assertEquals(BaseYangTypes.STRING_TYPE, unionLeafProperty.getReturnType());
                                    } else if (unionLeafProperty.getName().equals("uint8")) {
                                        uint8UnionPropertyFound = true;
                                        assertEquals(BaseYangTypes.UINT8_TYPE, unionLeafProperty.getReturnType());
                                    }
                                }
                                assertTrue(int32UnionPropertyFound);
                                assertTrue(stringUnionPropertyFound);
                                assertTrue(uint8UnionPropertyFound);
                            }
                        }
                    }
                }
            }
        }
        assertTrue(parentContainerFound);
        assertTrue(bitLeafTOFound);
        assertTrue(unionLeafTOFound);
    }
}
