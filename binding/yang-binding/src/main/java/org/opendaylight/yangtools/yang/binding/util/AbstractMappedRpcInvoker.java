/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractMappedRpcInvoker<T> extends RpcServiceInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMappedRpcInvoker.class);
    private final Map<T, RpcMethodInvoker> map;

    protected AbstractMappedRpcInvoker(final Map<T, Method> map) {
        final Builder<T, RpcMethodInvoker> b = ImmutableMap.builder();

        for (Entry<T, Method> e : map.entrySet()) {
            if (BindingReflections.isRpcMethod(e.getValue())) {
                b.put(e.getKey(), RpcMethodInvoker.from(e.getValue()));
            } else {
                LOG.debug("Method {} is not an RPC method, ignoring it", e.getValue());
            }
        }

        this.map = b.build();
    }

    protected abstract T qnameToKey(QName qname);

    @Override
    public final Future<RpcResult<?>> invokeRpc(@Nonnull final RpcService impl, @Nonnull final QName rpcName,
            @Nullable final DataObject input) {
        Preconditions.checkNotNull(impl, "Implementation must be supplied");

        RpcMethodInvoker invoker = map.get(qnameToKey(rpcName));
        Preconditions.checkArgument(invoker != null, "Supplied RPC is not valid for implementation %s", impl);
        return invoker.invokeOn(impl, input);
    }
}
