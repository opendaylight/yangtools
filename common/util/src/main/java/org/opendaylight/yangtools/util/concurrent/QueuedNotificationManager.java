/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link NotificationManager} that is pessimistic about listener type and uses identity mapping for comparing them.
 * This is defensive versus reused objects, maintaining semantics. This may not always be intended, for example if
 * {@code L} is a {@code String} which is being dynamically determined. In that case we do not want to use identity, but
 * equality, as otherwise the caller is forced to use {@link String#intern()} -- leading to interning in lookup, which
 * is absolutely unnecessary. In such use cases, use {@link EqualityQueuedNotificationManager} instead.
 *
 * @author Thomas Pantelis
 */
public final class QueuedNotificationManager<L, N> extends IdentityQueuedNotificationManager<L, N> {
    @FunctionalInterface
    public interface BatchedInvoker<L, N> {
        /**
         * Called to invoke a listener with a notification.
         *
         * @param listener the listener to invoke
         * @param notifications notifications to send
         */
        void invokeListener(@NonNull L listener, @NonNull ImmutableList<N> notifications);
    }

    QueuedNotificationManager(final @NonNull Executor executor, final @NonNull BatchedInvoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final @NonNull String name) {
        super(name, executor, maxQueueCapacity, listenerInvoker);
    }

    /**
     * Create a new notification manager.
     *
     * @param executor the {@link Executor} to use for notification tasks
     * @param listenerInvoker the {@link BatchedInvoker} to use for invoking listeners
     * @param maxQueueCapacity the capacity of each listener queue
     * @param name the name of this instance for logging info
     */
    public static <L, N> QueuedNotificationManager<L, N> create(final @NonNull Executor executor,
            final@NonNull  BatchedInvoker<L, N> listenerInvoker, final int maxQueueCapacity,
            final @NonNull String name) {
        return new QueuedNotificationManager<>(executor, listenerInvoker, maxQueueCapacity, name);
    }
}
