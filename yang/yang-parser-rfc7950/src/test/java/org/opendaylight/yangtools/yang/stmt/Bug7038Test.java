/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug7038Test {
    @Test
    public void unknownNodeTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug7038");
        assertNotNull(context);
        assertEquals(1, context.getUnknownSchemaNodes().size());
    }

    @Test
    public void testYang11() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug7038/yang11");
        assertNotNull(context);
    }

    @Test
    public void testYang10() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug7038/yang10");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("REQUIRE_INSTANCE is not valid for TYPE"));
        }
    }
}
