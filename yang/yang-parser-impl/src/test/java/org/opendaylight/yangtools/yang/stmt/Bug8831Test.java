/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug8831Test {
    @Test
    public void test() throws Exception {
        final SchemaContext context = TestUtils.parseYangSources("/bugs/bug8831/valid");
        assertNotNull(context);
    }

    @Test
    public void invalidModelsTest() throws Exception {
        try {
            TestUtils.parseYangSource("/bugs/bug8831/invalid/inv-model.yang");
            fail("Test should fails due to invalid yang 1.1 model");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(
                    e.getCause().getMessage().contains("has default value 'any' marked with an if-feature statement"));
        }
    }

    @Test
    public void invalidModelsTest2() throws Exception {
        try {
            TestUtils.parseYangSource("/bugs/bug8831/invalid/inv-model2.yang");
            fail("Test should fails due to invalid yang 1.1 model");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(
                    e.getCause().getMessage().contains("has default value 'any' marked with an if-feature statement"));
        }
    }
}
