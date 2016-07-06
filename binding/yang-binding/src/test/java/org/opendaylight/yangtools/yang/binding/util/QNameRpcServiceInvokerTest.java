/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class QNameRpcServiceInvokerTest {

    @Test
    public void instanceForTest() throws Exception {
        assertNotNull(QNameRpcServiceInvoker.instanceFor(ImmutableMap.of()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void qnameToKeyTest() throws Exception {
        final RpcService rpcService = mock(RpcService.class);
        QNameRpcServiceInvoker.instanceFor(ImmutableMap.of()).invokeRpc(rpcService, QName.create("test"), null);
        fail("Expected exception: constructed with empty map");
    }
}