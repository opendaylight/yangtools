/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Bug6410Test extends AbstractYangTest {
    @Test
    void testTypedefsInRpc() {
        final var schemaContext = assertEffectiveModel("/bugs/bug6410/foo.yang");

        final var modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final var module = modules.iterator().next();

        final var rpcs = module.getRpcs();
        assertEquals(1, rpcs.size());
        final var rpc = rpcs.iterator().next();

        final var typeDefs = rpc.getTypeDefinitions();
        assertEquals(2, typeDefs.size());
    }

    @Test
    void shouldFailOnDuplicateTypedefs() {
        assertSourceException(containsString("Duplicate name for typedef"), "/bugs/bug6410/bar.yang");
    }
}
