/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class IdentityrefTypeTest {

    private static List<File> testModels = null;

    @Before
    public void loadTestResources() throws URISyntaxException {
        URI folderPath = IdentityrefTypeTest.class.getResource("/identityref.yang").toURI();
        File folderFile = new File(folderPath);
        testModels = new ArrayList<>();

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
     * Test mainly for the method TypeProviderImpl#provideTypeForIdentityref(IdentityrefTypeDefinition)
     * provideTypeForIdentityref}.
     */
    @Test
    public void testIdentityrefYangBuiltInType() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangFiles(testModels));
        assertEquals(2, genTypes.size());

        GeneratedType moduleGenType = genTypes.stream()
            .filter(type -> type.getName().equals("ModuleIdentityrefData"))
            .findFirst()
            .orElseThrow();

        List<MethodSignature> methodSignatures = moduleGenType.getMethodDefinitions();
        assertEquals(2, methodSignatures.size());

        MethodSignature methodSignature = methodSignatures.get(0);
        assertEquals("getLf", methodSignature.getName());
        assertEquals("requireLf", methodSignatures.get(1).getName());

        Type returnType = methodSignature.getReturnType();
        assertThat(returnType, instanceOf(ParameterizedType.class));
        ParameterizedType parameterized = (ParameterizedType) returnType;
        assertEquals(Types.CLASS, parameterized.getRawType());

        Type[] actualTypes = parameterized.getActualTypeArguments();
        assertEquals("Incorrect number of type parameters", 1, actualTypes.length);
        assertEquals("Return type has incorrect actual parameter",
            "org.opendaylight.yang.gen.v1.urn.identityref.module.rev131109.SomeIdentity",
            actualTypes[0].getFullyQualifiedName());
    }
}
