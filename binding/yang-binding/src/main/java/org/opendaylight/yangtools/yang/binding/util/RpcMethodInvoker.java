/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

abstract class RpcMethodInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    abstract ListenableFuture<RpcResult<?>> invokeOn(RpcService impl, DataObject input);

    protected static RpcMethodInvoker from(final Method method) {
        final MethodHandle methodHandle;
        try {
            methodHandle = LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.",e);
        }

        final Optional<Class<? extends DataContainer>> input = BindingReflections.resolveRpcInputClass(method);
        if (input.isPresent()) {
            return new RpcMethodInvokerWithInput(methodHandle);
        }
        return new RpcMethodInvokerWithoutInput(methodHandle);
    }
}