/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class GeneratedTypesStringTest {

    private final static List<File> testModels = new ArrayList<File>();

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File listModelFile = new File(GeneratedTypesStringTest.class.getResource("/simple-string-demo.yang")
                .toURI());
        testModels.add(listModelFile);
    }

    @Test
    public void constantGenerationTest() throws IOException, SourceException, ReactorException {
        final SchemaContext context =  RetestUtils.parseYangSources(testModels);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context);

        boolean typedefStringFound = false;
        boolean constantRegExListFound = false;
        boolean constantRegExListTypeGeneric = false;
        boolean constantRegExListTypeContainer = false;
        boolean noStringInReqExListFound = false;
        boolean constantRegExListValueOK = false;
        boolean constantRegExListTypeOneGeneric = false;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;

                if (genTO.getName().equals("TypedefString")) {
                    typedefStringFound = true;

                    List<Constant> constants = genTO.getConstantDefinitions();
                    for (Constant con : constants) {
                        if (con.getName().equals("PATTERN_CONSTANTS")) {
                            constantRegExListFound = true;
                        } else {
                            break;
                        }
                        ParameterizedType pType;
                        if (con.getType() instanceof ParameterizedType) {
                            pType = (ParameterizedType) con.getType();
                        } else {
                            break;
                        }

                        Type[] types;
                        if (pType.getName().equals("List")) {
                            constantRegExListTypeContainer = true;
                            types = pType.getActualTypeArguments();
                        } else {
                            break;
                        }

                        if (types.length == 1) {
                            constantRegExListTypeOneGeneric = true;
                        } else {
                            break;
                        }

                        if (types[0].getName().equals("String")) {
                            constantRegExListTypeGeneric = true;
                        } else {
                            break;
                        }

                        if (con.getValue() instanceof List) {
                            constantRegExListValueOK = true;
                        } else {
                            break;
                        }

                        for (Object obj : (List<?>) con.getValue()) {
                            if (!(obj instanceof String)) {
                                noStringInReqExListFound = true;
                                break;
                            }
                        }

                    }
                }
            }

        }

        assertTrue("Typedef >>TypedefString<< wasn't found", typedefStringFound);
        assertTrue("Constant PATTERN_CONSTANTS is missing in TO", constantRegExListFound);
        assertTrue("Constant PATTERN_CONSTANTS doesn't have correct container type", constantRegExListTypeContainer);
        assertTrue("Constant PATTERN_CONSTANTS has more than one generic type", constantRegExListTypeOneGeneric);
        assertTrue("Constant PATTERN_CONSTANTS doesn't have correct generic type", constantRegExListTypeGeneric);
        assertTrue("Constant PATTERN_CONSTANTS doesn't contain List object", constantRegExListValueOK);
        assertTrue("In list found other type than String", !noStringInReqExListFound);

    }

}
