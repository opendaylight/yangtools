/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Provides single method invocation of RPCs on supplied instance.
 *
 * Rpc Service invoker provides common invocation interface for any subtype of {@link RpcService}.
 * via {@link #invokeNotification(RpcService, QName, DataObject)} method.
 *
 *
 *
 */
public final class NotificationListenerInvoker {

    private static Lookup LOOKUP = MethodHandles.lookup();

    private static final LoadingCache<Class<? extends NotificationListener>, NotificationListenerInvoker> INVOKERS = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<Class<? extends NotificationListener>, NotificationListenerInvoker>() {

                @Override
                public NotificationListenerInvoker load(Class<? extends NotificationListener> key) throws Exception {
                    return createInvoker(key);
                }

            });

    private final Map<QName, MethodHandle> methodInvokers;

    public NotificationListenerInvoker(Map<QName, MethodHandle> map) {
        this.methodInvokers  = map;
    }

    /**
     *
     * Creates RPCServiceInvoker for specified RpcService type
     *
     * @param type RpcService interface, which was generated from model.
     * @return Cached instance of {@link NotificationListenerInvoker} for supplied RPC type.
     *
     */
    public static NotificationListenerInvoker from(Class<? extends NotificationListener> type) {
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl Imlementation on which notifiaction callback should be invoked.
     * @param rpcName Name of RPC to be invoked.
     * @param input Input data for RPC.
     *
     */
    public void invokeNotification(@Nonnull NotificationListener impl, @Nonnull QName rpcName,@Nullable DataContainer input ) {
        Preconditions.checkNotNull(impl, "implemetation must be supplied");
        MethodHandle invoker = methodInvokers.get(rpcName);
        Preconditions.checkArgument(invoker != null,"Supplied notification is not valid for implementation %s",impl);
        try {
            invoker.invokeExact(impl, input);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    private static NotificationListenerInvoker createInvoker(Class<? extends NotificationListener> key) {
        return new NotificationListenerInvoker(createInvokerMap(key));
    }

    private static Map<QName, MethodHandle> createInvokerMap(Class<? extends NotificationListener> key) {
        Builder<QName, MethodHandle> ret = ImmutableMap.<QName, MethodHandle>builder();
        for(Method method : key.getMethods()) {
            if(BindingReflections.isNotificationCallback(method)) {

                Class<?> notification = method.getParameterTypes()[0];
                QName name = BindingReflections.findQName(notification);
                MethodHandle handle;
                try {
                    handle = LOOKUP.unreflect(method).asType(MethodType.methodType(Void.class, NotificationListener.class, DataContainer.class));
                    ret.put(name, handle);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Can not access public method.",e);
                }
            }

        }
        return ret.build();
    }

}
