/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ForwardingIdentityObject;

/**
 * This class manages queuing and dispatching notifications for multiple listeners concurrently.
 * Notifications are queued on a per-listener basis and dispatched serially to each listener via an
 * {@link Executor}.
 *
 * <p>This class optimizes its memory footprint by only allocating and maintaining a queue and executor
 * task for a listener when there are pending notifications. On the first notification(s), a queue
 * is created and a task is submitted to the executor to dispatch the queue to the associated
 * listener. Any subsequent notifications that occur before all previous notifications have been
 * dispatched are appended to the existing queue. When all notifications have been dispatched, the
 * queue and task are discarded.
 *
 * @author Thomas Pantelis
 *
 * @param <L> the listener type
 * @param <N> the notification type
 */
public final class QueuedNotificationManager<L, N> extends AbstractBatchingExecutor<ForwardingIdentityObject<L>, N>
        implements NotificationManager<L, N> {
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

    private final @NonNull QueuedNotificationManagerMXBean mxBean = new QueuedNotificationManagerMXBeanImpl(this);
    private final @NonNull BatchedInvoker<L, N> listenerInvoker;

    QueuedNotificationManager(final @NonNull Executor executor, final @NonNull BatchedInvoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final @NonNull String name) {
        super(name, executor, maxQueueCapacity);
        this.listenerInvoker = requireNonNull(listenerInvoker);
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

    /**
     * Returns the maximum listener queue capacity.
     */
    public int getMaxQueueCapacity() {
        return maxQueueCapacity();
    }

    /**
     * Return an {@link QueuedNotificationManagerMXBean} tied to this instance.
     *
     * @return An QueuedNotificationManagerMXBean object.
     */
    public @NonNull QueuedNotificationManagerMXBean getMXBean() {
        return mxBean;
    }

    /**
     * Returns the {@link Executor} to used for notification tasks.
     */
    public @NonNull Executor getExecutor() {
        return executor();
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#addNotification(L, N)
     */
    @Override
    public void submitNotification(final L listener, final N notification) {
        if (listener != null && notification != null) {
            submitTask(ForwardingIdentityObject.of(listener), notification);
        }
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#submitNotifications(L, java.util.Collection)
     */
    @Override
    public void submitNotifications(final L listener, final Iterable<N> notifications) {
        if (listener != null && notifications != null) {
            submitTasks(ForwardingIdentityObject.of(listener), notifications);
        }
    }

    /**
     * Returns {@link ListenerNotificationQueueStats} instances for each current listener
     * notification task in progress.
     */
    public List<ListenerNotificationQueueStats> getListenerNotificationQueueStats() {
        return streamTasks().map(t -> new ListenerNotificationQueueStats(t.key().toString(), t.size()))
                .collect(Collectors.toList());
    }

    @Override
    void executeBatch(final ForwardingIdentityObject<L> key, final ImmutableList<N> tasks) {
        listenerInvoker.invokeListener(key.getDelegate(), tasks);
    }
}
