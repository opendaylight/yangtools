/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

final class ClassBasedRpcServiceInvoker extends AbstractMappedRpcInvoker<String> {
    private static final LoadingCache<Class<? extends RpcService>, RpcServiceInvoker> INVOKERS = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<Class<? extends RpcService>, RpcServiceInvoker>() {
                @Override
                public RpcServiceInvoker load(final Class<? extends RpcService> key) {
                    final Map<String, Method> ret = new HashMap<>();
                    for (Method m : key.getMethods()) {
                        ret.put(m.getName(), m);
                    }

                    return new ClassBasedRpcServiceInvoker(ret);
                }
            });

    ClassBasedRpcServiceInvoker(final Map<String, Method> ret) {
        super(ret);
    }

    @Override
    protected String qnameToKey(final QName qname) {
        return BindingMapping.getMethodName(qname);
    }

    static RpcServiceInvoker instanceFor(final Class<? extends RpcService> type) {
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }
}
