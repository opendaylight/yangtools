/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class QueuedNotificationManager<L, N> implements NotificationManager<L, N> {

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
        void invokeListener(@Nonnull L listener, @Nonnull Collection<? extends N> notifications);
    }

    private static final Logger LOG = LoggerFactory.getLogger(QueuedNotificationManager.class);

    /**
     * Caps the maximum number of attempts to offer notification to a particular listener.  Each
     * attempt window is 1 minute, so an offer times out after roughly 10 minutes.
     */
    private static final int MAX_NOTIFICATION_OFFER_MINUTES = 10;
    private static final long GIVE_UP_NANOS = TimeUnit.MINUTES.toNanos(MAX_NOTIFICATION_OFFER_MINUTES);
    private static final long TASK_WAIT_NANOS = TimeUnit.MILLISECONDS.toNanos(10);

    private final ConcurrentMap<ListenerKey<L>, NotificationTask> listenerCache = new ConcurrentHashMap<>();
    private final BatchedInvoker<L, N> listenerInvoker;
    private final Executor executor;
    private final String name;
    private final int maxQueueCapacity;

    private QueuedNotificationManager(final Executor executor, final BatchedInvoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final String name) {
        checkArgument(maxQueueCapacity > 0, "Invalid maxQueueCapacity %s must be > 0", maxQueueCapacity);
        this.executor = requireNonNull(executor);
        this.listenerInvoker = requireNonNull(listenerInvoker);
        this.maxQueueCapacity = maxQueueCapacity;
        this.name = requireNonNull(name);
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
    public QueuedNotificationManager(final Executor executor, final Invoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final String name) {
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
    public static <L, N> QueuedNotificationManager<L, N> create(final Executor executor,
            final BatchedInvoker<L, N> listenerInvoker, final int maxQueueCapacity, final String name) {
        return new QueuedNotificationManager<>(executor, listenerInvoker, maxQueueCapacity, name);
    }

    /**
     * Returns the maximum listener queue capacity.
     */
    public int getMaxQueueCapacity() {
        return maxQueueCapacity;
    }

    /**
     * Returns the {@link Executor} to used for notification tasks.
     */
    public Executor getExecutor() {
        return executor;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#addNotification(L, N)
     */
    @Override
    public void submitNotification(final L listener, final N notification) throws RejectedExecutionException {
        if (notification != null) {
            submitNotifications(listener, Collections.singletonList(notification));
        }
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#submitNotifications(L, java.util.Collection)
     */
    @Override
    public void submitNotifications(final L listener, final Iterable<N> notifications)
            throws RejectedExecutionException {

        if (notifications == null || listener == null) {
            return;
        }

        LOG.trace("{}: submitNotifications for listener {}: {}", name, listener, notifications);

        final ListenerKey<L> key = new ListenerKey<>(listener);

        // Keep looping until we are either able to add a new NotificationTask or are able to
        // add our notifications to an existing NotificationTask. Eventually one or the other
        // will occur.
        try {
            Iterator<N> it = notifications.iterator();

            while (true) {
                NotificationTask task = listenerCache.get(key);
                if (task == null) {
                    // No task found, try to insert a new one
                    final NotificationTask newTask = new NotificationTask(key, it);
                    task = listenerCache.putIfAbsent(key, newTask);
                    if (task == null) {
                        // We were able to put our new task - now submit it to the executor and
                        // we're done. If it throws a RejectedExecutionException, let that propagate
                        // to the caller.
                        runTask(listener, newTask);
                        break;
                    }

                    // We have a racing task, hence we can continue, but we need to refresh our iterator from
                    // the task.
                    it = newTask.recoverItems();
                }

                final boolean completed = task.submitNotifications(it);
                if (!completed) {
                    // Task is indicating it is exiting before it has consumed all the items and is exiting. Rather
                    // than spinning on removal, we try to replace it.
                    final NotificationTask newTask = new NotificationTask(key, it);
                    if (listenerCache.replace(key, task, newTask)) {
                        runTask(listener, newTask);
                        break;
                    }

                    // We failed to replace the task, hence we need retry. Note we have to recover the items to be
                    // published from the new task.
                    it = newTask.recoverItems();
                    LOG.debug("{}: retrying task queueing for {}", name, listener);
                    continue;
                }

                // All notifications have either been delivered or we have timed out and warned about the ones we
                // have failed to deliver. In any case we are done here.
                break;
            }
        } catch (InterruptedException e) {
            // We were interrupted trying to offer to the listener's queue. Somebody's probably
            // telling us to quit.
            LOG.warn("{}: Interrupted trying to add to {} listener's queue", name, listener);
        }

        LOG.trace("{}: submitNotifications done for listener {}", name, listener);
    }

    /**
     * Returns {@link ListenerNotificationQueueStats} instances for each current listener
     * notification task in progress.
     */
    public List<ListenerNotificationQueueStats> getListenerNotificationQueueStats() {
        return listenerCache.values().stream().map(t -> new ListenerNotificationQueueStats(t.listenerKey.toString(),
            t.size())).collect(Collectors.toList());
    }

    private void runTask(final L listener, final NotificationTask task) {
        LOG.debug("{}: Submitting NotificationTask for listener {}", name, listener);
        executor.execute(task);
    }

    /**
     * Used as the listenerCache map key. We key by listener reference identity hashCode/equals.
     * Since we don't know anything about the listener class implementations and we're mixing
     * multiple listener class instances in the same map, this avoids any potential issue with an
     * equals implementation that just blindly casts the other Object to compare instead of checking
     * for instanceof.
     */
    private static final class ListenerKey<L> {
        private final L listener;

        ListenerKey(final L listener) {
            this.listener = requireNonNull(listener);
        }

        L getListener() {
            return listener;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(listener);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof ListenerKey<?> && listener == ((ListenerKey<?>) obj).listener;
        }

        @Override
        public String toString() {
            return listener.toString();
        }
    }

    /**
     * Executor task for a single listener that queues notifications and sends them serially to the
     * listener.
     */
    private class NotificationTask implements Runnable {

        private final Lock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();
        private final ListenerKey<L> listenerKey;

        @GuardedBy("lock")
        private final Queue<N> queue = new ArrayDeque<>();
        @GuardedBy("lock")
        private boolean exiting;

        NotificationTask(final ListenerKey<L> listenerKey, final Iterator<N> notifications) {
            this.listenerKey = requireNonNull(listenerKey);
            while (notifications.hasNext()) {
                queue.offer(notifications.next());
            }
        }

        Iterator<N> recoverItems() {
            // This violates @GuardedBy annotation, but is invoked only when the task is not started and will never
            // get started, hence this is safe.
            return queue.iterator();
        }

        int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }

        boolean submitNotifications(final Iterator<N> notifications) throws InterruptedException {
            final long start = System.nanoTime();
            final long deadline = start + GIVE_UP_NANOS;

            lock.lock();
            try {
                // Lock may have blocked for some time, we need to take that into account. We may have exceedded
                // the deadline, but that is unlikely and even in that case we can make some progress without further
                // blocking.
                long canWait = deadline - System.nanoTime();

                while (true) {
                    // Check the exiting flag - if true then #run is in the process of exiting so return
                    // false to indicate such. Otherwise, offer the notifications to the queue.
                    if (exiting) {
                        return false;
                    }

                    final int avail = maxQueueCapacity - queue.size();
                    if (avail <= 0) {
                        if (canWait <= 0) {
                            LOG.warn("{}: Failed to offer notifications {} to the queue for listener {}. Exceeded"
                                + "maximum allowable time of {} minutes; the listener is likely in an unrecoverable"
                                + "state (deadlock or endless loop). ", name, ImmutableList.copyOf(notifications),
                                listenerKey, MAX_NOTIFICATION_OFFER_MINUTES);
                            return true;
                        }

                        canWait = notFull.awaitNanos(canWait);
                        continue;
                    }

                    for (int i = 0; i < avail; ++i) {
                        if (!notifications.hasNext()) {
                            notEmpty.signal();
                            return true;
                        }

                        queue.offer(notifications.next());
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @GuardedBy("lock")
        private boolean waitForQueue() {
            long timeout = TASK_WAIT_NANOS;

            while (queue.isEmpty()) {
                if (timeout <= 0) {
                    return false;
                }

                try {
                    timeout = notEmpty.awaitNanos(timeout);
                } catch (InterruptedException e) {
                    // The executor is probably shutting down so log as debug.
                    LOG.debug("{}: Interrupted trying to remove from {} listener's queue", name, listenerKey);
                    return false;
                }
            }

            return true;
        }

        @Override
        public void run() {
            try {
                // Loop until we've dispatched all the notifications in the queue.
                while (true) {
                    final Collection<N> notifications;

                    lock.lock();
                    try {
                        if (!waitForQueue()) {
                            exiting = true;
                            break;
                        }

                        // Splice the entire queue
                        notifications = ImmutableList.copyOf(queue);
                        queue.clear();

                        notFull.signalAll();
                    } finally {
                        lock.unlock();
                    }

                    invokeListener(notifications);
                }
            } finally {
                // We're exiting, gracefully or not - either way make sure we always remove
                // ourselves from the cache.
                listenerCache.remove(listenerKey, this);
            }
        }

        @SuppressWarnings("checkstyle:illegalCatch")
        private void invokeListener(final Collection<N> notifications) {
            LOG.debug("{}: Invoking listener {} with notification: {}", name, listenerKey, notifications);
            try {
                listenerInvoker.invokeListener(listenerKey.getListener(), notifications);
            } catch (Exception e) {
                // We'll let a RuntimeException from the listener slide and keep sending any remaining notifications.
                LOG.error("{}: Error notifying listener {} with {}", name, listenerKey, notifications, e);
            }
        }
    }
}
