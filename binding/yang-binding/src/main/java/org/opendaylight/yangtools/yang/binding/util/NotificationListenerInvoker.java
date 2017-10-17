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
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Provides single method invocation of notificatoin callbacks on supplied instance.
 *
 * <p>
 * Notification Listener invoker provides common invocation interface for any subtype of {@link NotificationListener}.
 * via {@link #invokeNotification(NotificationListener, QName, DataContainer)} method.
 */
public final class NotificationListenerInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    private static final LoadingCache<Class<? extends NotificationListener>, NotificationListenerInvoker> INVOKERS =
            CacheBuilder .newBuilder().weakKeys()
            .build(new CacheLoader<Class<? extends NotificationListener>, NotificationListenerInvoker>() {

                private NotificationListenerInvoker createInvoker(
                        final Class<? extends NotificationListener> key) {
                    return new NotificationListenerInvoker(createInvokerMap(key));
                }

                private Map<QName, MethodHandle> createInvokerMap(final Class<? extends NotificationListener> key) {
                    final Builder<QName, MethodHandle> ret = ImmutableMap.builder();
                    for (final Method method : key.getMethods()) {
                        if (BindingReflections.isNotificationCallback(method)) {

                            final Class<?> notification = method.getParameterTypes()[0];
                            final QName name = BindingReflections.findQName(notification);
                            MethodHandle handle;
                            try {
                                handle = LOOKUP.unreflect(method).asType(MethodType.methodType(void.class,
                                    NotificationListener.class, DataContainer.class));
                                ret.put(name, handle);
                            } catch (final IllegalAccessException e) {
                                throw new IllegalStateException("Can not access public method.", e);
                            }
                        }

                    }
                    return ret.build();
                }

                @Override
                public NotificationListenerInvoker load(final Class<? extends NotificationListener> key) {
                    return createInvoker(key);
                }

            });

    private final Map<QName, MethodHandle> methodInvokers;

    public NotificationListenerInvoker(final Map<QName, MethodHandle> map) {
        this.methodInvokers = map;
    }

    /**
     * Creates RPCServiceInvoker for specified RpcService type.
     *
     * @param type
     *            RpcService interface, which was generated from model.
     * @return Cached instance of {@link NotificationListenerInvoker} for
     *         supplied RPC type.
     */
    public static NotificationListenerInvoker from(final Class<? extends NotificationListener> type) {
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl
     *            Imlementation on which notifiaction callback should be
     *            invoked.
     * @param rpcName
     *            Name of RPC to be invoked.
     * @param input
     *            Input data for RPC.
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    public void invokeNotification(@Nonnull final NotificationListener impl, @Nonnull final QName rpcName,
            @Nullable final DataContainer input) {
        Preconditions.checkNotNull(impl, "implemetation must be supplied");
        final MethodHandle invoker = methodInvokers.get(rpcName);
        Preconditions.checkArgument(invoker != null, "Supplied notification is not valid for implementation %s", impl);
        try {
            invoker.invokeExact(impl, input);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}
