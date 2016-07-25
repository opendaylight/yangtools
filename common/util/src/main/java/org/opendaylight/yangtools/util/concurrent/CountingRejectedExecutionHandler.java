/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import com.google.common.base.Preconditions;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.opendaylight.yangtools.util.ExecutorServiceUtil;

/**
 * A RejectedExecutionHandler that delegates to a backing RejectedExecutionHandler and counts the
 * number of rejected tasks.
 *
 * @author Thomas Pantelis
 */
public class CountingRejectedExecutionHandler implements RejectedExecutionHandler {
    private static final AtomicLongFieldUpdater<CountingRejectedExecutionHandler> COUNTER_UPDATER =
            AtomicLongFieldUpdater.newUpdater(CountingRejectedExecutionHandler.class, "rejectedTaskCounter");
    private final RejectedExecutionHandler delegate;
    private volatile long rejectedTaskCounter;

    /**
     * Constructor.
     *
     * @param delegate the backing RejectedExecutionHandler.
     */
    public CountingRejectedExecutionHandler( final RejectedExecutionHandler delegate ) {
        this.delegate = Preconditions.checkNotNull( delegate );
    }

    @Override
    public void rejectedExecution( final Runnable task, final ThreadPoolExecutor executor ) {
        COUNTER_UPDATER.incrementAndGet(this);
        delegate.rejectedExecution( task, executor );
    }

    /**
     * Returns the rejected task count.
     */
    public long getRejectedTaskCount() {
        return rejectedTaskCounter;
    }

    /**
     * Returns s counting handler for rejected tasks that runs the rejected task directly in the
     * calling thread of the execute method, unless the executor has been shut down, in which case
     * the task is discarded.
     */
    public static CountingRejectedExecutionHandler newCallerRunsPolicy() {
        return new CountingRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
    }

    /**
     * Returns a counting handler for rejected tasks that throws a RejectedExecutionException.
     */
    public static CountingRejectedExecutionHandler newAbortPolicy() {
        return new CountingRejectedExecutionHandler( new ThreadPoolExecutor.AbortPolicy() );
    }

    /**
     * Returns a counting handler for rejected tasks that that blocks on the
     * {@link ThreadPoolExecutor}'s backing queue until it can add the task to the queue.
     */
    public static CountingRejectedExecutionHandler newCallerWaitsPolicy() {
        return new CountingRejectedExecutionHandler( ExecutorServiceUtil.waitInQueueExecutionHandler() );
    }
}
