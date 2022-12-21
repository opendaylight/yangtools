/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class Bug6410Test {

    @Test
    void testTypedefsInRpc() throws ReactorException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(sourceForResource("/bugs/bug6410/foo.yang"));

        final Collection<? extends Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module module = modules.iterator().next();

        final Collection<? extends RpcDefinition> rpcs = module.getRpcs();
        assertEquals(1, rpcs.size());
        final RpcDefinition rpc = rpcs.iterator().next();

        final Collection<? extends TypeDefinition<?>> typeDefs = rpc.getTypeDefinitions();
        assertEquals(2, typeDefs.size());
    }

    @Test
    void shouldFailOnDuplicateTypedefs() {
        try {
            StmtTestUtils.parseYangSources(sourceForResource("/bugs/bug6410/bar.yang"));
            fail("A ReactorException should have been thrown.");
        } catch (ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().contains("Duplicate name for typedef"));
        }
    }
}
