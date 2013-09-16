/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class IdentityrefTypeTest {

    private static List<File> testModels = null;

    @Before
    public void loadTestResources() {
        String folderPath = IdentityrefTypeTest.class.getResource("/identityref.yang").getPath();
        File folderFile = new File(folderPath);
        testModels = new ArrayList<File>();

        if (folderFile.isFile()) {
            testModels.add(folderFile);
        } else {
            for (File file : folderFile.listFiles()) {
                if (file.isFile()) {
                    testModels.add(file);
                }
            }
        }
    }

    /**
     * Test mainly for the method
     * {@link TypeProviderImpl#provideTypeForIdentityref()
     * provideTypeForIdentityref}
     */
    @Test
    public void testIdentityrefYangBuiltInType() {
        loadTestResources();
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        GeneratedType moduleGenType = null;
        for (Type type : genTypes) {
            if (type.getName().equals("ModuleIdentityrefData")) {
                if (type instanceof GeneratedType) {
                    moduleGenType = (GeneratedType) type;
                }
            }
        }

        assertNotNull("Generated type for whole module wasn't found", moduleGenType);

        String typeName = null;
        String actualTypeName = "";
        int numOfActualTypes = 0;
        List<MethodSignature> methodSignatures = moduleGenType.getMethodDefinitions();
        for (MethodSignature methodSignature : methodSignatures) {
            if (methodSignature.getName().equals("getLf")) {
                Type returnType = methodSignature.getReturnType();
                if (returnType instanceof ParameterizedType) {
                    typeName = returnType.getName();
                    Type[] actualTypes = ((ParameterizedType) returnType).getActualTypeArguments();
                    numOfActualTypes = actualTypes.length;
                    actualTypeName = actualTypes[0].getName();
                }
            }
        }
        assertNotNull("The method 'getLf' wasn't found", typeName);
        assertEquals("Return type has incorrect name", "Class", typeName);
        assertEquals("Incorrect number of type parameters", 1, numOfActualTypes);
        assertEquals("Return type has incorrect actual parameter", "SomeIdentity", actualTypeName);

    }

}
