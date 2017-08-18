/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.LongAdder;
import org.opendaylight.yangtools.util.ExecutorServiceUtil;

/**
 * A RejectedExecutionHandler that delegates to a backing RejectedExecutionHandler and counts the
 * number of rejected tasks.
 *
 * @author Thomas Pantelis
 */
public class CountingRejectedExecutionHandler implements RejectedExecutionHandler {
    private final LongAdder rejectedTaskCounter = new LongAdder();
    private final RejectedExecutionHandler delegate;

    /**
     * Constructor.
     *
     * @param delegate the backing RejectedExecutionHandler.
     */
    public CountingRejectedExecutionHandler(final RejectedExecutionHandler delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
        rejectedTaskCounter.increment();
        delegate.rejectedExecution(task, executor);
    }

    /**
     * Returns the rejected task count.
     */
    public long getRejectedTaskCount() {
        return rejectedTaskCounter.sum();
    }

    /**
     * Returns s counting handler for rejected tasks that runs the rejected task directly in the
     * calling thread of the execute method, unless the executor has been shut down, in which case
     * the task is discarded.
     */
    public static CountingRejectedExecutionHandler newCallerRunsPolicy() {
        return new CountingRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Returns a counting handler for rejected tasks that throws a RejectedExecutionException.
     */
    public static CountingRejectedExecutionHandler newAbortPolicy() {
        return new CountingRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Returns a counting handler for rejected tasks that that blocks on the
     * {@link ThreadPoolExecutor}'s backing queue until it can add the task to the queue.
     */
    public static CountingRejectedExecutionHandler newCallerWaitsPolicy() {
        return new CountingRejectedExecutionHandler(ExecutorServiceUtil.waitInQueueExecutionHandler());
    }
}
