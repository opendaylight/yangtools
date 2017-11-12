/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug7424Test {
    @Test
    public void testRpc() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-rpc.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Error in module 'foo': cannot add '(foo)name'. Node name collision: '(foo)name' already declared"));
        }
    }

    @Test
    public void testNotification() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-notification.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Error in module 'foo': cannot add '(foo)name'. Node name collision: '(foo)name' already declared"));
        }
    }

    @Test
    public void testData() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-data.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Error in module 'foo': cannot add '(foo)name'. Node name collision: '(foo)name' already declared"));
        }
    }

    @Test
    public void testRpcUses() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-rpc-uses.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                "Error in module 'foo': cannot add '(foo)name'. Node name collision: '(foo)name' already declared"));
        }
    }
}
