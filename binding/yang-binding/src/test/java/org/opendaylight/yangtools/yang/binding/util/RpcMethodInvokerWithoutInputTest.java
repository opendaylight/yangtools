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
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcMethodInvokerWithoutInputTest {

    private static final TestImplClassWithoutInput TEST_IMPL_CLASS = new TestImplClassWithoutInput();

    @Test
    public void invokeOnTest() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(
                TestImplClassWithoutInput.class.getDeclaredMethod("testMethod", RpcService.class));
        final RpcMethodInvokerWithoutInput invokerWithoutInput = new RpcMethodInvokerWithoutInput(methodHandle);
        assertNotNull(invokerWithoutInput.invokeOn(TEST_IMPL_CLASS, null));
    }

    @Test(expected = InternalError.class)
    public void invokeOnWithException() throws Exception {
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(
                TestImplClassWithoutInput.class.getDeclaredMethod("testMethodWithException", RpcService.class));
        final RpcMethodInvokerWithoutInput invokerWithoutInput = new RpcMethodInvokerWithoutInput(methodHandle);
        invokerWithoutInput.invokeOn(TEST_IMPL_CLASS, null);
    }

    private static final class TestImplClassWithoutInput implements RpcService {

        @SuppressWarnings("unused")
        static ListenableFuture<?> testMethod(final RpcService testArgument) {
            return Futures.immediateFuture(null);
        }

        @SuppressWarnings("unused")
        static ListenableFuture<?> testMethodWithException(final RpcService testArgument) throws Exception {
            throw new InternalError();
        }
    }
}