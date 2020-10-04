/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6871Test {

    @Test
    public void testValidYang11Model() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6871/foo.yang");
        assertNotNull(schemaContext);

        final Module foo = schemaContext.findModule("foo", Revision.of("2016-12-14")).get();

        final Collection<? extends NotificationDefinition> notifications = foo.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition myNotification = notifications.iterator().next();
        Collection<? extends MustDefinition> mustConstraints = myNotification.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final Collection<? extends RpcDefinition> rpcs = foo.getRpcs();
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
    public void testInvalidYang10Model() throws Exception {
        assertException("/rfc7950/bug6871/foo10.yang", "MUST is not valid for NOTIFICATION");
        assertException("/rfc7950/bug6871/bar10.yang", "MUST is not valid for INPUT");
        assertException("/rfc7950/bug6871/baz10.yang", "MUST is not valid for OUTPUT");
    }

    private static void assertException(final String sourcePath, final String exceptionMessage) throws Exception {
        try {
            StmtTestUtils.parseYangSource(sourcePath);
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException ex) {
            assertTrue(ex.getCause().getMessage().startsWith(exceptionMessage));
        }
    }
}
