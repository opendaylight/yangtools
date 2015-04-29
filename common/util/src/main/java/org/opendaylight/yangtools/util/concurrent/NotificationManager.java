/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.concurrent.RejectedExecutionException;

/**
 * Interface for a class that manages queuing and dispatching notifications for multiple listeners.
 *
 * @author Thomas Pantelis
 *
 * @param <L> the listener type
 * @param <N> the notification type
 */
public interface NotificationManager<L, N> {

    /**
     * Submits a notification to be queued and dispatched to the given listener.
     * <p>
     * <b>Note:</b> This method may block if the listener queue is currently full.
     *
     * @param listener the listener to notify
     * @param notification the notification to dispatch
     * @throws RejectedExecutionException if the notification can't be queued for dispatching
     */
    void submitNotification( L listener, N notification );

    /**
     * Submits notifications to be queued and dispatched to the given listener.
     * <p>
     * <b>Note:</b> This method may block if the listener queue is currently full.
     *
     * @param listener the listener to notify
     * @param notifications the notifications to dispatch
     * @throws RejectedExecutionException if a notification can't be queued for dispatching
     */
    void submitNotifications( final L listener, Iterable<N> notifications);

}