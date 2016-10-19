/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class Bug6961Test {

    @Test
    public void testBug6961SchemaContext() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6961/");
        assertNotNull(context);
        final Set<ModuleIdentifier> allModuleIdentifiers = context.getAllModuleIdentifiers();
        assertNotNull(allModuleIdentifiers);
        assertEquals(6, allModuleIdentifiers.size());
        final SchemaContext schemaContext = EffectiveSchemaContext.resolveSchemaContext(context.getModules());
        assertNotNull(schemaContext);
        assertEquals(6, schemaContext.getAllModuleIdentifiers().size());
    }
}