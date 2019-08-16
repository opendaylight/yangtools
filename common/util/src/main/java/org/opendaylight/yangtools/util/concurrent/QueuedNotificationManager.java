/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@inheritDoc}
 *
 * <p>
 * This class is pessimistic about listener type and uses identity mapping for comparing them. This is defensive versus
 * reused objects, maintaining semantics. This may not always be intended, for example if {@code L} is a {@code String}
 * which is being dynamically determined. In that case we do not want to use identity, but equality, as otherwise
 * the caller is forced to use {@link String#intern()} -- leading to interning in lookup, which is absolutely
 * unnecessary. In such use cases, use {@link EqualityQueuedNotificationManager} instead.
 *
 * @author Thomas Pantelis
 */
public final class QueuedNotificationManager<L, N> extends IdentityQueuedNotificationManager<L, N> {

    /**
     * Interface implemented by clients that does the work of invoking listeners with notifications.
     *
     * @author Thomas Pantelis
     *
     * @param <L> the listener type
     * @param <N> the notification type
     *
     * @deprecated Use {@link QueuedNotificationManager.BatchedInvoker} instead.
     */
    @Deprecated
    @FunctionalInterface
    public interface Invoker<L, N> {
        /**
         * Called to invoke a listener with a notification.
         *
         * @param listener the listener to invoke
         * @param notification the notification to send
         */
        void invokeListener(L listener, N notification);
    }

    @FunctionalInterface
    public interface BatchedInvoker<L, N> {
        /**
         * Called to invoke a listener with a notification.
         *
         * @param listener the listener to invoke
         * @param notifications notifications to send
         */
        void invokeListener(@NonNull L listener, @NonNull Collection<? extends N> notifications);
    }

    private static final Logger LOG = LoggerFactory.getLogger(QueuedNotificationManager.class);

    QueuedNotificationManager(final @NonNull Executor executor, final @NonNull BatchedInvoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final @NonNull String name) {
        super(name, executor, maxQueueCapacity, listenerInvoker);
    }

    /**
     * Constructor.
     *
     * @param executor the {@link Executor} to use for notification tasks
     * @param listenerInvoker the {@link Invoker} to use for invoking listeners
     * @param maxQueueCapacity the capacity of each listener queue
     * @param name the name of this instance for logging info
     *
     * @deprecated Use {@link #create(Executor, BatchedInvoker, int, String)} instead.
     */
    @Deprecated
    @SuppressWarnings("checkstyle:illegalCatch")
    public QueuedNotificationManager(final @NonNull Executor executor, final @NonNull Invoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final @NonNull String name) {
        this(executor, (BatchedInvoker<L, N>)(listener, notifications) -> notifications.forEach(n -> {
            try {
                listenerInvoker.invokeListener(listener, n);
            } catch (Exception e) {
                LOG.error("{}: Error notifying listener {} with {}", name, listener, n, e);
            }

        }), maxQueueCapacity, name);
        requireNonNull(listenerInvoker);
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
