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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;

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
@NonNullByDefault
abstract class AbstractQueuedNotificationManager<T, L, N> extends AbstractBatchingExecutor<T, N>
        implements NotificationManager<L, N> {

    private final QueuedNotificationManagerMXBean mxBean = new QueuedNotificationManagerMXBeanImpl(this);
    private final BatchedInvoker<L, N> listenerInvoker;

    AbstractQueuedNotificationManager(final String name, final Executor executor, final int maxQueueCapacity,
            final BatchedInvoker<L, N> listenerInvoker) {
        super(name, executor, maxQueueCapacity);
        this.listenerInvoker = requireNonNull(listenerInvoker);
    }

    /**
     * Returns the {@link Executor} to used for notification tasks.
     */
    public final Executor getExecutor() {
        return executor();
    }

    /**
     * Returns the maximum listener queue capacity.
     */
    public final int getMaxQueueCapacity() {
        return maxQueueCapacity();
    }

    /**
     * Return an {@link QueuedNotificationManagerMXBean} tied to this instance.
     *
     * @return An QueuedNotificationManagerMXBean object.
     */
    public final QueuedNotificationManagerMXBean getMXBean() {
        return mxBean;
    }

    /**
     * Returns {@link ListenerNotificationQueueStats} instances for each current listener
     * notification task in progress.
     */
    // FIXME: drop visibility to package-protected
    public final List<ListenerNotificationQueueStats> getListenerNotificationQueueStats() {
        return streamTasks().map(t -> new ListenerNotificationQueueStats(t.key().toString(), t.size()))
                .collect(Collectors.toList());
    }

    @Override
    public final void submitNotification(final L listener, final N notification) {
        if (listener != null && notification != null) {
            submitTask(wrap(listener), notification);
        }
    }

    @Override
    public final void submitNotifications(final L listener, final @Nullable Iterable<N> notifications) {
        if (listener != null && notifications != null) {
            submitTasks(wrap(listener), notifications);
        }
    }

    @Override
    final void executeBatch(final T key, final @NonNull ImmutableList<N> tasks) {
        listenerInvoker.invokeListener(unwrap(key), tasks);
    }

    abstract T wrap(L listener);

    abstract L unwrap(T key);
}
