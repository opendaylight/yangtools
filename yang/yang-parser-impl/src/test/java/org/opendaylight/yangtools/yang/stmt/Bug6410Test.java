/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class Bug6410Test {

    @Test
    public void testTypedefsInRpc() throws ReactorException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                new YangStatementSourceImpl("/bugs/bug6410/foo.yang", false));

        final Set<Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module module = modules.iterator().next();

        final Set<RpcDefinition> rpcs = module.getRpcs();
        assertEquals(1, rpcs.size());
        final RpcDefinition rpc = rpcs.iterator().next();

        final Set<TypeDefinition<?>> typeDefs = rpc.getTypeDefinitions();
        assertEquals(2, typeDefs.size());
    }

    @Test
    public void shouldFailOnDuplicateTypedefs() {
        try {
            StmtTestUtils.parseYangSources(new YangStatementSourceImpl("/bugs/bug6410/bar.yang", false));
            fail("A ReactorException should have been thrown.");
        } catch (ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().contains("Duplicate name for typedef"));
        }
    }
}
