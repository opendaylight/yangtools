/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertNotNull;

import com.google.common.util.concurrent.Futures;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcMethodInvokerWithInputTest {

    private static final TestImplClassWithInput TEST_IMPL_CLASS = new TestImplClassWithInput();

    @Test
    public void invokeOnTest() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(
                TestImplClassWithInput.class.getDeclaredMethod("testMethod", RpcService.class, DataObject.class));
        final RpcMethodInvokerWithInput rpcMethodInvokerWithInput = new RpcMethodInvokerWithInput(methodHandle);
        assertNotNull(rpcMethodInvokerWithInput.invokeOn(TEST_IMPL_CLASS, null));
    }

    @Test(expected = InternalError.class)
    public void invokeOnWithException() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(TestImplClassWithInput.class
                .getDeclaredMethod("testMethodWithException", RpcService.class, DataObject.class));
        final RpcMethodInvokerWithInput rpcMethodInvokerWithInput = new RpcMethodInvokerWithInput(methodHandle);
        rpcMethodInvokerWithInput.invokeOn(TEST_IMPL_CLASS, null);
    }

    private static final class TestImplClassWithInput implements RpcService {

        static Future testMethod(RpcService testArg, DataObject data) {
            return Futures.immediateFuture(null);
        }

        static Future testMethodWithException(RpcService testArg, DataObject data) throws Exception {
            throw new InternalError();
        }
    }
}