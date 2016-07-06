/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class LocalNameRpcServiceInvokerTest {

    private static RpcServiceInvoker rpcServiceInvoker;
    private static final QNameModule Q_NAME_MODULE =
            QNameModule.create(URI.create("testURI"), new Date(System.currentTimeMillis()));
    private static final RpcService RPC_SERVICE = mock(RpcService.class);

    @BeforeClass
    public static void setUp() throws Exception {
        rpcServiceInvoker = LocalNameRpcServiceInvoker.instanceFor(
                Q_NAME_MODULE, ImmutableMap.of(QName.create("test"), Object.class.getDeclaredMethod("hashCode")));

        assertNotNull(rpcServiceInvoker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void qnameToKeyTest() throws Exception {
        rpcServiceInvoker.invokeRpc(RPC_SERVICE, QName.create(Q_NAME_MODULE, "test"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void qnameToKeyWithNullTest() throws Exception {
        rpcServiceInvoker.invokeRpc(RPC_SERVICE, QName.create("test"), null);
    }
}