/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class IdentityrefTypeTest {
    private List<File> testModels;

    @BeforeEach
    void loadTestResources() throws Exception {
        final var folderFile = Path.of(IdentityrefTypeTest.class.getResource("/identityref.yang").toURI()).toFile();
        testModels = new ArrayList<>();

        if (folderFile.isFile()) {
            testModels.add(folderFile);
        } else {
            for (var file : folderFile.listFiles()) {
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
    void testIdentityrefYangBuiltInType() {
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
