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

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GenTypesSubSetTest {
    @Test
    void genTypesFromSubsetOfTwoModulesTest() {
        final var context = YangParserTestUtils.parseYangResources(GenTypesSubSetTest.class,
                "/leafref-test-models/abstract-topology@2013-02-08.yang", "/ietf-models/ietf-interfaces.yang",
                "/ietf-models/ietf-inet-types.yang", "/ietf-models/ietf-yang-types.yang");
        final var toGenModules = new HashSet<Module>();
        for (var module : context.getModules()) {
            if (module.getName().equals("abstract-topology") || module.getName().equals("ietf-interfaces")) {
                toGenModules.add(module);
            }
        }

        assertEquals(2, toGenModules.size(), "Set of to Generate Modules must contain 2 modules");
        final var genTypes = DefaultBindingGenerator.generateFor(context, toGenModules);
        assertNotNull(genTypes, "genTypes is null");
        assertEquals(25, genTypes.size());
    }

    @Test
    void genTypesFromSubsetOfThreeModulesTest() {
        final var context = YangParserTestUtils.parseYangResources(GenTypesSubSetTest.class,
                "/leafref-test-models/abstract-topology@2013-02-08.yang", "/ietf-models/ietf-interfaces.yang",
                "/ietf-models/ietf-inet-types.yang", "/ietf-models/ietf-yang-types.yang",
                "/ietf-models/iana-if-type.yang");
        assertNotNull(context);

        final var toGenModules = new HashSet<Module>();
        for (var module : context.getModules()) {
            if (module.getName().equals("abstract-topology") || module.getName().equals("ietf-interfaces")
                || module.getName().equals("iana-if-type")) {
                toGenModules.add(module);
            }
        }
        assertEquals(3, toGenModules.size(), "Set of to Generate Modules must contain 3 modules");

        final var genTypes = DefaultBindingGenerator.generateFor(context, toGenModules);
        assertNotNull(genTypes, "genTypes is null");
        assertEquals(27, genTypes.size());
    }
}
