/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
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
    private static final int MAX_NOTIFICATION_OFFER_ATTEMPTS = 10;

    private final ConcurrentMap<ListenerKey<L>, NotificationTask> listenerCache = new ConcurrentHashMap<>();
    private final BatchedInvoker<L, N> listenerInvoker;
    private final Executor executor;
    private final String name;
    private final int maxQueueCapacity;

    private QueuedNotificationManager(final Executor executor, final BatchedInvoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final String name) {
        Preconditions.checkArgument(maxQueueCapacity > 0, "Invalid maxQueueCapacity %s must be > 0", maxQueueCapacity);
        this.executor = Preconditions.checkNotNull(executor);
        this.listenerInvoker = Preconditions.checkNotNull(listenerInvoker);
        this.maxQueueCapacity = maxQueueCapacity;
        this.name = Preconditions.checkNotNull(name);
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
    public QueuedNotificationManager(final Executor executor, final Invoker<L, N> listenerInvoker,
            final int maxQueueCapacity, final String name) {
        this(executor, (BatchedInvoker<L, N>)(l, c) -> c.forEach(n -> listenerInvoker.invokeListener(l, n)),
            maxQueueCapacity, name);
        Preconditions.checkNotNull(listenerInvoker);
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
            NotificationTask newNotificationTask = null;

            while (true) {
                final NotificationTask existingTask = listenerCache.get(key);
                if (existingTask != null && existingTask.submitNotifications(notifications)) {
                    // We were able to add our notifications to an existing task so we're done.
                    break;
                }

                // Either there's no existing task or we couldn't add our notifications to the
                // existing one because it's in the process of exiting and removing itself from
                // the cache. Either way try to put a new task in the cache. If we can't put
                // then either the existing one is still there and hasn't removed itself quite
                // yet or some other concurrent thread beat us to the put although this method
                // shouldn't be called concurrently for the same listener as that would violate
                // notification ordering. In any case loop back up and try again.

                if (newNotificationTask == null) {
                    newNotificationTask = new NotificationTask(key, notifications);
                }
                final NotificationTask oldTask = listenerCache.putIfAbsent(key, newNotificationTask);
                if (oldTask == null) {
                    // We were able to put our new task - now submit it to the executor and
                    // we're done. If it throws a RejectedxecutionException, let that propagate
                    // to the caller.

                    LOG.debug("{}: Submitting NotificationTask for listener {}", name, listener);
                    executor.execute(newNotificationTask);
                    break;
                }

                LOG.debug("{}: retrying task queueing for {}", name, listener);
            }
        } catch (InterruptedException e) {
            // We were interrupted trying to offer to the listener's queue. Somebody's probably
            // telling us to quit.
            LOG.warn("{}: Interrupted trying to add to {} listener's queue", name, listener);
        }

        LOG.trace("{}: submitNotifications dine for listener {}", name, listener);
    }

    /**
     * Returns {@link ListenerNotificationQueueStats} instances for each current listener
     * notification task in progress.
     */
    public List<ListenerNotificationQueueStats> getListenerNotificationQueueStats() {
        return listenerCache.values().stream().map(t -> new ListenerNotificationQueueStats(t.listenerKey.toString(),
            t.notificationQueue.size())).collect(Collectors.toList());
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
            this.listener = Preconditions.checkNotNull(listener);
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
            return (obj instanceof ListenerKey<?>) && listener == ((ListenerKey<?>) obj).listener;
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
        private final Lock queuingLock = new ReentrantLock();
        private final BlockingQueue<N> notificationQueue;
        private final ListenerKey<L> listenerKey;

        @GuardedBy("queuingLock")
        private boolean queuedNotifications = false;
        private volatile boolean done = false;

        NotificationTask(final ListenerKey<L> listenerKey, final Iterable<N> notifications) {
            this.listenerKey = Preconditions.checkNotNull(listenerKey);
            this.notificationQueue = new LinkedBlockingQueue<>(maxQueueCapacity);

            for (N notification: notifications) {
                this.notificationQueue.add(notification);
            }
        }

        @GuardedBy("queuingLock")
        private void publishNotification(final N notification) throws InterruptedException {
            // The offer is attempted for up to 10 minutes, with a status message printed each minute
            for (int notificationOfferAttempts = 0;
                 notificationOfferAttempts < MAX_NOTIFICATION_OFFER_ATTEMPTS; notificationOfferAttempts++) {

                // Try to offer for up to a minute and log a message if it times out.
                LOG.debug("{}: Offering notification to the queue for listener {}: {}", name, listenerKey,
                    notification);

                if (notificationQueue.offer(notification, 1, TimeUnit.MINUTES)) {
                    return;
                }

                LOG.warn("{}: Timed out trying to offer a notification to the queue for listener {} "
                        + "on attempt {} of {}. The queue has reached its capacity of {}", name, listenerKey,
                        notificationOfferAttempts, MAX_NOTIFICATION_OFFER_ATTEMPTS, maxQueueCapacity);
            }

            LOG.warn("{}: Failed to offer a notification to the queue for listener {}. Exceeded max allowable attempts"
                    + " of {} in {} minutes; the listener is likely in an unrecoverable state (deadlock or endless"
                    + " loop).", name, listenerKey, MAX_NOTIFICATION_OFFER_ATTEMPTS, MAX_NOTIFICATION_OFFER_ATTEMPTS);
        }

        boolean submitNotifications(final Iterable<N> notifications) throws InterruptedException {

            queuingLock.lock();
            try {

                // Check the done flag - if true then #run is in the process of exiting so return
                // false to indicate such. Otherwise, offer the notifications to the queue.

                if (done) {
                    return false;
                }

                for (N notification : notifications) {
                    publishNotification(notification);
                }

                // Set the queuedNotifications flag to tell #run that we've just queued
                // notifications and not to exit yet, even if it thinks the queue is empty at this
                // point.

                queuedNotifications = true;
            } finally {
                queuingLock.unlock();
            }

            return true;
        }

        @Override
        public void run() {
            try {
                // Loop until we've dispatched all the notifications in the queue.

                while (true) {
                    // Get the notification at the head of the queue, waiting a little bit for one
                    // to get offered.

                    final N notification = notificationQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (notification == null) {

                        // The queue is empty - try to get the queuingLock. If we can't get the lock
                        // then #submitNotifications is in the process of offering to the queue so
                        // we'll loop back up and poll the queue again.

                        if (queuingLock.tryLock()) {
                            try {

                                // Check the queuedNotifications flag to see if #submitNotifications
                                // has offered new notification(s) to the queue. If so, loop back up
                                // and poll the queue again. Otherwise set done to true and exit.
                                // Once we set the done flag and unlock, calls to
                                // #submitNotifications will fail and a new task will be created.

                                if (!queuedNotifications) {
                                    done = true;
                                    break;
                                }

                                // Clear the queuedNotifications flag so we'll try to exit the next
                                // time through the loop when the queue is empty.

                                queuedNotifications = false;

                            } finally {
                                queuingLock.unlock();
                            }
                        }
                    }

                    notifyListener(notification);
                }
            } catch (InterruptedException e) {
                // The executor is probably shutting down so log as debug.
                LOG.debug("{}: Interrupted trying to remove from {} listener's queue", name, listenerKey);
            } finally {
                // We're exiting, gracefully or not - either way make sure we always remove
                // ourselves from the cache.
                listenerCache.remove(listenerKey, this);
            }
        }

        private void notifyListener(final N notification) {
            if (notification == null) {
                return;
            }

            LOG.debug("{}: Invoking listener {} with notification: {}", name, listenerKey, notification);
            try {
                listenerInvoker.invokeListener(listenerKey.getListener(), ImmutableList.of(notification));
            } catch (Exception e) {
                // We'll let a RuntimeException from the listener slide and keep sending any remaining notifications.
                LOG.error(String.format("%1$s: Error notifying listener %2$s", name, listenerKey), e);
            }
        }
    }
}
