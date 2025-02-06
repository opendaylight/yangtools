/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesBitsTest {
    @Test
    void testGeneretedTypesBitsTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-bits-demo.yang"));
        assertNotNull(genTypes);

        List<MethodSignature> methodSignaturesList = null;

        boolean leafParentFound = false;

        boolean byteTypeFound = false;
        int classPropertiesNumb = 0;
        int toStringPropertiesNum = 0;
        int equalPropertiesNum = 0;
        int hashPropertiesNum = 0;

        String nameReturnParamType = "";
        boolean getByteLeafMethodFound = false;
        boolean setByteLeafMethodFound = false;
        int setByteLeafMethodParamNum = 0;

        for (var genType : genTypes) {
            if (genType instanceof GeneratedTransferObject genTO) {
                if (genTO.getName().equals("ByteType")) {
                    byteTypeFound = true;
                    var genProperties = genTO.getProperties();
                    classPropertiesNumb = genProperties.size();

                    genProperties = genTO.getToStringIdentifiers();
                    toStringPropertiesNum = genProperties.size();

                    genProperties = genTO.getEqualsIdentifiers();
                    equalPropertiesNum = genProperties.size();

                    genProperties = genTO.getHashCodeIdentifiers();
                    hashPropertiesNum = genProperties.size();

                }
            } else if (genType.getName().equals("LeafParentContainer")) {
                leafParentFound = true;
                // check of methods
                methodSignaturesList = genType.getMethodDefinitions();
                if (methodSignaturesList != null) {
                    // loop through all methods
                    for (var methodSignature : methodSignaturesList) {
                        if (methodSignature.getName().equals("getByteLeaf")) {
                            getByteLeafMethodFound = true;

                            nameReturnParamType = methodSignature.getReturnType().getName();
                        } else if (methodSignature.getName().equals("setByteLeaf")) {
                            setByteLeafMethodFound = true;

                            List<Parameter> parameters = methodSignature.getParameters();
                            setByteLeafMethodParamNum = parameters.size();
                        }
                    }
                }
            }
        }

        assertTrue(byteTypeFound);

        assertEquals(8, classPropertiesNumb);

        assertEquals(8, toStringPropertiesNum);
        assertEquals(8, equalPropertiesNum);
        assertEquals(8, hashPropertiesNum);
        assertTrue(leafParentFound);

        assertNotNull(methodSignaturesList);

        assertTrue(getByteLeafMethodFound);
        assertEquals("ByteType", nameReturnParamType);

        assertFalse(setByteLeafMethodFound);
        assertEquals(0, setByteLeafMethodParamNum);
    }
}
