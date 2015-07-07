/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import java.net.URI;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class GeneratedTypesBitsTest {


    @Test
    public void testGeneretedTypesBitsTest() throws Exception {
        final URI yangTypesPath = getClass().getResource("/simple-bits-demo.yang").toURI();

        final SchemaContext context =  RetestUtils.parseYangSources(new File(yangTypesPath));
        assertTrue(context != null);

        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context);
        assertTrue(genTypes != null);

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

        for (final Type type : genTypes) {
            if (type instanceof GeneratedTransferObject) { // searching for
                                                           // ByteType
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
                if (genTO.getName().equals("ByteType")) {
                    byteTypeFound = true;
                    List<GeneratedProperty> genProperties = genTO.getProperties();
                    classPropertiesNumb = genProperties.size();

                    genProperties = null;
                    genProperties = genTO.getToStringIdentifiers();
                    toStringPropertiesNum = genProperties.size();

                    genProperties = null;
                    genProperties = genTO.getEqualsIdentifiers();
                    equalPropertiesNum = genProperties.size();

                    genProperties = null;
                    genProperties = genTO.getHashCodeIdentifiers();
                    hashPropertiesNum = genProperties.size();

                }
            } else if (type instanceof GeneratedType) { // searching for
                                                        // interface
                                                        // LeafParameterContainer
                final GeneratedType genType = (GeneratedType) type;
                if (genType.getName().equals("LeafParentContainer")) {
                    leafParentFound = true;
                    // check of methods
                    methodSignaturesList = genType.getMethodDefinitions();
                    if (methodSignaturesList != null) {
                        for (MethodSignature methodSignature : methodSignaturesList) { // loop
                                                                                       // through
                                                                                       // all
                                                                                       // methods
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

        }

        assertTrue(byteTypeFound);

        assertEquals(classPropertiesNumb,8);

        assertEquals(toStringPropertiesNum,8);
        assertEquals(equalPropertiesNum,8);
        assertEquals(hashPropertiesNum,8);
        assertTrue(leafParentFound);

        assertNotNull(methodSignaturesList);

        assertTrue(getByteLeafMethodFound);
        assertEquals(nameReturnParamType,"ByteType");

        assertFalse(setByteLeafMethodFound);
        assertEquals(0, setByteLeafMethodParamNum);
    }

}
