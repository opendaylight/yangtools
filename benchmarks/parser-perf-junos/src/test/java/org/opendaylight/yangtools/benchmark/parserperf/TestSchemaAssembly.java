/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.parserperf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TestSchemaAssembly {
    @Test
    public void testAssemblyJunos() {
        processModels("/junos", 35);
    }

    @Test
    public void testAssemblyNCS6K() {
        processModels("/ncs6k", 546);
    }

    @Test
    public void testAssemblyNE40E() {
        processModels("/ne40e", 48);
    }

    private static void processModels(final String subdir, final int expectedModules) {
        final EffectiveModelContext result = YangParserTestUtils.parseYangResourceDirectory(subdir);
        assertEquals(expectedModules, result.getModules().size());
    }
}
