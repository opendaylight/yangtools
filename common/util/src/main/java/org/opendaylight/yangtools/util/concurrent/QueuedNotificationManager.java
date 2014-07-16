/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages queuing and dispatching notifications for multiple listeners concurrently.
 * Notifications are queued on a per-listener basis and dispatched serially to each listener via an
 * {@link Executor}.
 * <p>
 * This class optimizes its memory footprint by only allocating and maintaining a queue and executor
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
public class QueuedNotificationManager<L,N> implements NotificationManager<L,N> {

    /**
     * Interface implemented by clients that does the work of invoking listeners with notifications.
     *
     * @author Thomas Pantelis
     *
     * @param <L> the listener type
     * @param <N> the notification type
     */
    public interface Invoker<L,N> {

        /**
         * Called to invoke a listener with a notification.
         *
         * @param listener the listener to invoke
         * @param notification the notification to send
         */
        void invokeListener( L listener, N notification );
    }

    private static final Logger LOG = LoggerFactory.getLogger( QueuedNotificationManager.class );

    private final Executor executor;
    private final Invoker<L,N> listenerInvoker;

    private final ConcurrentMap<ListenerKey<L>,NotificationTask<L,N>>
                                                          listenerCache = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param executor the {@link Executor} to use for notification tasks
     * @param listenerInvoker the {@link Invoker} to use for invokings listeners
     */
    public QueuedNotificationManager( Executor executor, Invoker<L,N> listenerInvoker ) {
        this.executor = executor;
        this.listenerInvoker = listenerInvoker;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#addNotification(L, N)
     */
    @Override
    public void addNotification( final L listener, final N notification ) {
        if( notification == null ) {
            return;
        }

        addNotifications( listener, Arrays.asList( notification ) );
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.util.concurrent.NotificationManager#addNotifications(L, java.util.Collection)
     */
    @Override
    public void addNotifications( final L listener, final Collection<N> notifications ) {

        if( notifications == null || listener == null ) {
            return;
        }

        ListenerKey<L> key = new ListenerKey<>( listener );
        NotificationTask<L,N> newNotificationTask = null;

        // Keep looping until we are either able to add a new NotificationTask or are able to
        // add our notifications to an existing NotificationTask. Eventually one or the other
        // will occur.

        while( true ) {
            NotificationTask<L,N> existingTask = listenerCache.get( key );

            if( existingTask == null || !existingTask.addNotifications( notifications ) ) {

                // Either there's no existing task or we couldn't add our notifications to the
                // existing one because it's in the process of exiting and removing itself from the
                // cache. Either way try to put a new task in the cache. If we can't put then either
                // the existing one is still there and hasn't removed itself quite yet or some other
                // concurrent thread beat us to the put. Either way we'll loop back up and try again.

                if( newNotificationTask == null ) {
                    newNotificationTask = new NotificationTask<>(
                                               key, notifications, listenerCache, listenerInvoker );
                }

                existingTask = listenerCache.putIfAbsent( key, newNotificationTask );
                if( existingTask == null ) {

                    // We were able to put our new task - now submit it to the executor and
                    // we're done.

                    executor.execute( newNotificationTask );
                    break;
                }
            } else {

                // We were able to add our notifications to an existing task so we're done.

                break;
            }
        }
    }

    private static class ListenerKey<L> {

        private final L listener;

        public ListenerKey( L listener ) {
            this.listener = listener;
        }

        L getListener() {
            return listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            ListenerKey<?> other = (ListenerKey<?>) obj;
            return listener == other.listener;
        }
    }

    /**
     * Executor task for a single listener that queues notifications and sends them serially to the
     * listener.
     */
    private static class NotificationTask<L,N> implements Runnable {

        @GuardedBy("notificationQueue")
        private final Queue<N> notificationQueue;

        private volatile boolean done = false;

        private final Invoker<L,N> listenerInvoker;
        private final ListenerKey<L> listenerKey;
        private final ConcurrentMap<ListenerKey<L>,NotificationTask<L,N>> listenerCache;

        NotificationTask( ListenerKey<L> listenerKey, Collection<N> notifications,
                ConcurrentMap<ListenerKey<L>,NotificationTask<L,N>> listenerCache,
                Invoker<L, N> listenerInvoker) {

            this.listenerKey = listenerKey;
            this.notificationQueue = new LinkedList<>( notifications );
            this.listenerInvoker = listenerInvoker;
            this.listenerCache = listenerCache;
        }

        boolean addNotifications( Collection<N> notifications ) {

            boolean result = false;
            synchronized( notificationQueue ) {

                // Check the done flag - if true then #run is in the process of exiting so return
                // false to indicate such. Otherwise, add to the queue. We're thread-safe here
                // because we hold the notificationQueue lock.

                if( !done ) {
                    notificationQueue.addAll( notifications );
                    result = true;
                }
            }

            return result;
        }

        @Override
        public void run() {

            try {
                // Loop until we've dispatched all the notifications in the queue.

                while( true ) {
                    N notification;
                    synchronized( notificationQueue ) {

                        // Get the notification at the head of the queue. If there's none, set done
                        // to true and exit. Once done is set and we leave the sync block,
                        // calls to #addNotifications will fail.

                        notification = notificationQueue.poll();
                        if( notification == null ) {
                            done = true;
                            break;
                        }
                    }

                    notifyListener( notification );
                }
            } finally {

                // We're exiting, gracefully or not - either way make sure we always remove
                // ourselves from  the cache.

                listenerCache.remove( listenerKey );
            }
        }

        private void notifyListener( N notification ) {

            try {

                listenerInvoker.invokeListener( listenerKey.getListener(), notification );

            } catch( RuntimeException e ) {

                // We'll let a RuntimeException from the listener slide and keep sending any
                // remaining notifications.

                LOG.error( "Error notifyng listener " + listenerKey.getListener().getClass(), e );

            } catch( Error e ) {

                // A JVM Error is severe - best practice is to throw them up the chain. Set done to
                // true so no new notifications can be added to this task as we're about to bail.

                done = true;
                throw e;
            }
        }
    }
}
