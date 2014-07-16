/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yangtools.util.ExecutorServiceUtil;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A ThreadPoolExecutor with a specified bounded queue capacity that handles tasks that are rejected
 * when all threads are busy by performing a blocking put to the queue. Therefore if the queue is
 * full, the caller submitting the task will be blocked until space becomes available on the queue.
 * <p>
 * This ThreadPoolExecutor favors creating new threads over queuing (the former is faster) so
 * threads will only be reused when the thread limit is exceeded and tasks are queued. Threads that
 * remain idle for 15 seconds are terminated. For example, if the maximum number of threads is 100
 * and 100 short-lived tasks are submitted within say 10 seconds, then 100 threads will be created
 * and used - existing idle threads will not be reused. This provides the fastest execution of the
 * 100 tasks at the expense of memory and thread resource overhead. Therefore it is advisable to
 * specify a relatively small thread limit (probably no more than 50).
 *
 * @author Thomas Pantelis
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final long IDLE_TIMEOUT_IN_SEC = 15L;

    private final String threadPrefix;
    private final int maximumQueueSize;

    /**
     * Constructor.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 15 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     */
    public BlockingThreadPoolExecutor( int maximumPoolSize, int maximumQueueSize, String threadPrefix ) {
        // We don't specify any core threads (first parameter) so, when a task is submitted,
        // the base class will always try to offer to the queue but we configure a queue that
        // doesn't allow tasks to be offered. Therefore, a new thread will be spawned to execute
        // the task. This is faster than handing the task to an existing idle thread via the queue.
        // Once the thread limit is reached, subsequent tasks will be queued. If the queue is full,
        // tasks will be rejected. We set a  RejectedExecutionHandler that does a blocking put to
        // the queue which will block the caller submitting the task. In this manner, we never
        // reject tasks.

        super( 0, maximumPoolSize, IDLE_TIMEOUT_IN_SEC, TimeUnit.SECONDS,
               ExecutorServiceUtil.offerFailingBlockingQueue(
                                new LinkedBlockingQueue<Runnable>( maximumQueueSize ) ),
               ExecutorServiceUtil.waitInQueueExecutionHandler() );

        this.threadPrefix = threadPrefix;
        this.maximumQueueSize = maximumQueueSize;

        setThreadFactory( new ThreadFactoryBuilder().setDaemon( true )
                                                 .setNameFormat( threadPrefix + "-%d" ).build() );

        // There's actually always 1 core thread even if you specify 0 so we'll allow it to
        // terminate when idle as well.
        allowCoreThreadTimeOut( true );
    }

    /**
     * Since this class uses an internal RejectedExecutionHandler, explicitly setting a
     * RejectedExecutionHandler is not allowed
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setRejectedExecutionHandler( RejectedExecutionHandler handler ) {
        throw new UnsupportedOperationException( "Setting RejectedExecutionHandler" );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( threadPrefix + ":" );
        builder.append( "\n\tCurrent Thread Pool Size: " ).append( getPoolSize() );
        builder.append( "\n\tLargest Thread Pool Size: " ).append( getLargestPoolSize() );
        builder.append( "\n\tMax Thread Pool Size: " ).append( getMaximumPoolSize() );
        builder.append( "\n\tCurrent Queue Size: " ).append( getQueue().size() );
        builder.append( "\n\tMax Queue Size: " ).append( maximumQueueSize );
        builder.append( "\n\tActive Thread Count: " ).append( getActiveCount() );
        builder.append( "\n\tCompleted Task Count: " ).append( getCompletedTaskCount() );
        builder.append( "\n\tTotal Task Count: " ).append( getTaskCount() );
        return builder.toString();
    }

}
