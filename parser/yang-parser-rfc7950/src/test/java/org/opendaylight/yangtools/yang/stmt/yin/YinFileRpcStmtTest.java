/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class YinFileRpcStmtTest extends AbstractYinModulesTest {
    @Test
    void testRpc() {
        final var testModule = context.findModules("ietf-netconf-monitoring").iterator().next();
        final var rpcs = testModule.getRpcs();
        assertEquals(1, rpcs.size());

        final var rpc = rpcs.iterator().next();
        assertEquals("get-schema", rpc.getQName().getLocalName());
        assertEquals(Optional.of("""
            This operation is used to retrieve a schema from the
            NETCONF server.

            Positive Response:
            The NETCONF server returns the requested schema.

            Negative Response:
            If requested schema does not exist, the <error-tag> is
            'invalid-value'.

            If more than one schema matches the requested parameters, the
            <error-tag> is 'operation-failed', and <error-app-tag> is
            'data-not-unique'."""), rpc.getDescription());

        final var input = rpc.getInput();
        assertNotNull(input);
        assertEquals(3, input.getChildNodes().size());

        final var output = rpc.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());
    }
}
