/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class ClassBasedRpcServiceInvokerTest {

    @Test
    public void qnameToKeyTest() throws Exception {
        final ClassBasedRpcServiceInvoker classBasedRpcServiceInvoker =
                new ClassBasedRpcServiceInvoker(ImmutableMap.of());
        assertNotNull(classBasedRpcServiceInvoker);
        assertNotNull(ClassBasedRpcServiceInvoker.instanceFor(TestInterface.class));
        assertEquals("localName", classBasedRpcServiceInvoker.qnameToKey(QName.create("testNamespace", "localName")));
    }

    interface TestInterface extends DataContainer, RpcService {}
}