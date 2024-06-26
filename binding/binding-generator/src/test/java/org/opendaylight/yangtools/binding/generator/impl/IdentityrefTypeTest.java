/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
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
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangFiles(testModels));
        assertEquals(2, genTypes.size());

        var moduleGenType = genTypes.stream()
            .filter(type -> type.getName().equals("ModuleIdentityrefData"))
            .findFirst()
            .orElseThrow();

        var methodSignatures = moduleGenType.getMethodDefinitions();
        assertEquals(3, methodSignatures.size());

        assertEquals("implementedInterface", methodSignatures.get(0).getName());
        var methodSignature = methodSignatures.get(1);
        assertEquals("getLf", methodSignature.getName());
        assertEquals("requireLf", methodSignatures.get(2).getName());

        assertEquals("org.opendaylight.yang.gen.v1.urn.identityref.module.rev131109.SomeIdentity",
            methodSignature.getReturnType().getFullyQualifiedName());
    }
}
