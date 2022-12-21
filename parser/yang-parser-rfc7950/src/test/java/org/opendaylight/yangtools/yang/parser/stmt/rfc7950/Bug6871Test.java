/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6871Test extends AbstractYangTest {
    @Test
    void testValidYang11Model() {
        final Module foo = assertEffectiveModel("/rfc7950/bug6871/foo.yang")
            .findModule("foo", Revision.of("2016-12-14")).orElseThrow();

        final var notifications = foo.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition myNotification = notifications.iterator().next();
        var mustConstraints = myNotification.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final var rpcs = foo.getRpcs();
        assertEquals(1, rpcs.size());
        final RpcDefinition myRpc = rpcs.iterator().next();

        final InputSchemaNode input = myRpc.getInput();
        assertNotNull(input);
        mustConstraints = input.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final OutputSchemaNode output = myRpc.getOutput();
        assertNotNull(output);
        mustConstraints = output.getMustConstraints();
        assertEquals(2, mustConstraints.size());
    }

    @Test
    void testInvalidYang10Model() {
        assertInvalidSubstatementException(startsWith("MUST is not valid for NOTIFICATION"),
            "/rfc7950/bug6871/foo10.yang");
        assertInvalidSubstatementException(startsWith("MUST is not valid for INPUT"), "/rfc7950/bug6871/bar10.yang");
        assertInvalidSubstatementException(startsWith("MUST is not valid for OUTPUT"), "/rfc7950/bug6871/baz10.yang");
    }
}
