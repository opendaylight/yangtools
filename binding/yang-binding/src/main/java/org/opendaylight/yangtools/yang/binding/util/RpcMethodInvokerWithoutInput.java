/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

class RpcMethodInvokerWithoutInput extends RpcMethodInvoker {

    private static final MethodType INVOCATION_SIGNATURE = MethodType.methodType(ListenableFuture.class,
        RpcService.class);
    private final MethodHandle handle;

    RpcMethodInvokerWithoutInput(final MethodHandle methodHandle) {
        this.handle = methodHandle.asType(INVOCATION_SIGNATURE);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    ListenableFuture<RpcResult<?>> invokeOn(final RpcService impl, final DataObject input) {
        try {
            return (ListenableFuture<RpcResult<?>>) handle.invokeExact(impl);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}