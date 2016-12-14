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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6871Test {

    @Test
    public void testValidYang11Model() throws ReactorException, FileNotFoundException, URISyntaxException,
            ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6871/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-12-14");

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);

        final Set<NotificationDefinition> notifications = foo.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition myNotification = notifications.iterator().next();
        Set<MustDefinition> mustConstraints = myNotification.getConstraints().getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final Set<RpcDefinition> rpcs = foo.getRpcs();
        assertEquals(1, rpcs.size());
        final RpcDefinition myRpc = rpcs.iterator().next();

        final ContainerSchemaNode input = myRpc.getInput();
        assertNotNull(input);
        mustConstraints = input.getConstraints().getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final ContainerSchemaNode output = myRpc.getOutput();
        assertNotNull(output);
        mustConstraints = output.getConstraints().getMustConstraints();
        assertEquals(2, mustConstraints.size());
    }

    @Test
    public void testInvalidYang10Model() throws FileNotFoundException, URISyntaxException {
        assertException("/rfc7950/bug6871/foo10.yang", "MUST is not valid for NOTIFICATION");
        assertException("/rfc7950/bug6871/bar10.yang", "MUST is not valid for INPUT");
        assertException("/rfc7950/bug6871/baz10.yang", "MUST is not valid for OUTPUT");
    }

    private static void assertException(final String sourcePath, final String exceptionMessage)
            throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource(sourcePath);
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException ex) {
            assertTrue(ex.getCause().getMessage().startsWith(exceptionMessage));
        }
    }
}
