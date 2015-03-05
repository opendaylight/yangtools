/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Provides single method invocation of RPCs on supplied instance.
 *
 * Rpc Service invoker provides common invocation interface for any subtype of {@link RpcService}.
 * via {@link #invokeRpc(RpcService, QName, DataObject)} method.
 *
 *
 *
 */
public final class RpcServiceInvoker {

    private static final LoadingCache<Class<? extends RpcService>, RpcServiceInvoker> INVOKERS = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<Class<? extends RpcService>, RpcServiceInvoker>() {

                @Override
                public RpcServiceInvoker load(Class<? extends RpcService> key) throws Exception {
                    return createInvoker(key);
                }

            });

    private final Map<String, RpcMethodInvoker> methodInvokers;

    private RpcServiceInvoker(Map<String, RpcMethodInvoker> methodInvokers) {
        this.methodInvokers  = Preconditions.checkNotNull(methodInvokers);
    }

    /**
     *
     * Creates RPCServiceInvoker for specified RpcService type
     *
     * @param type RpcService interface, which was generated from model.
     * @return Cached instance of {@link RpcServiceInvoker} for supplied RPC type.
     *
     */
    public static RpcServiceInvoker from(Class<? extends RpcService> type) {
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl Imlementation on which RPC should be invoked.
     * @param rpcName Name of RPC to be invoked.
     * @param input Input data for RPC.
     * @return Future which will complete once rpc procesing is finished.
     */
    public Future<RpcResult<?>> invokeRpc(@Nonnull RpcService impl, @Nonnull QName rpcName,@Nullable DataObject input ) {
        Preconditions.checkNotNull(impl, "implemetation must be supplied");
        return invoke(impl,BindingMapping.getMethodName(rpcName),input);
    }

    private static RpcServiceInvoker createInvoker(Class<? extends RpcService> key) {
        return new RpcServiceInvoker(createInvokerMap(key));
    }

    private static Map<String, RpcMethodInvoker> createInvokerMap(Class<? extends RpcService> key) {
        Builder<String, RpcMethodInvoker> ret = ImmutableMap.<String, RpcMethodInvoker>builder();
        for(Method method : key.getMethods()) {
            if(BindingReflections.isRpcMethod(method)) {
                ret.put(method.getName(), RpcMethodInvoker.from(method));
            }

        }
        return ret.build();
    }

    private Future<RpcResult<?>> invoke(RpcService impl, String methodName, DataObject input) {
        RpcMethodInvoker invoker = methodInvokers.get(methodName);
        Preconditions.checkArgument(invoker != null,"Supplied rpc is not valid for implementation %s",impl);
        return invoker.invokeOn(impl, input);
    }
}
