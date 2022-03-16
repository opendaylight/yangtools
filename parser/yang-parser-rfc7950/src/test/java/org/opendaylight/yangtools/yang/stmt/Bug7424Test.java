/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;

public class Bug7424Test extends AbstractYangTest {
    @Test
    public void testRpc()throws Exception {
        assertSourceException(startsWith("Error in module 'foo': cannot add '(foo)name'. Node name collision:"
                + " '(foo)name' already declared"), "/bugs/bug7424/foo-rpc.yang");
    }

    @Test
    public void testNotification() {
        assertSourceException(startsWith("Error in module 'foo': cannot add '(foo)name'. Node name collision:"
                + " '(foo)name' already declared"), "/bugs/bug7424/foo-notification.yang");
    }

    @Test
    public void testData() {
        assertSourceException(startsWith("Error in module 'foo': cannot add '(foo)name'. Node name collision:"
                + " '(foo)name' already declared"), "/bugs/bug7424/foo-data.yang");
    }

    @Test
    public void testRpcUses() {
        assertSourceException(startsWith("Error in module 'foo': cannot add '(foo)name'. Node name collision:"
                + " '(foo)name' already declared"), "/bugs/bug7424/foo-rpc-uses.yang");
    }
}
