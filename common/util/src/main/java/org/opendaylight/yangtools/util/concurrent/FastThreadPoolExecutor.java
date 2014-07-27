/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A ThreadPoolExecutor with a specified bounded queue capacity that favors creating new threads
 * over queuing, as the former is faster.
 * <p>
 * See {@link SpecialExecutors#newFastBlockingThreadPool} for more details.
 *
 * @author Thomas Pantelis
 */
public class FastThreadPoolExecutor extends ThreadPoolExecutor {

    private static final long DEFAULT_IDLE_TIMEOUT_IN_SEC = 15L;

    private final String threadPrefix;
    private final int maximumQueueSize;

    /**
     * Constructs a FastThreadPoolExecutor instance.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 15 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     */
    public FastThreadPoolExecutor( int maximumPoolSize, int maximumQueueSize, String threadPrefix ) {
        this( maximumPoolSize, maximumQueueSize, DEFAULT_IDLE_TIMEOUT_IN_SEC, TimeUnit.SECONDS,
              threadPrefix );
    }

    /**
     * Constructs a FastThreadPoolExecutor instance.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param keepAliveTime
     *            the maximum time that idle threads will wait for new tasks before terminating.
     * @param unit
     *            the time unit for the keepAliveTime argument
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     */
    public FastThreadPoolExecutor( int maximumPoolSize, int maximumQueueSize, long keepAliveTime,
            TimeUnit unit, String threadPrefix ) {
        // We use all core threads (the first 2 parameters below equal) so, when a task is submitted,
        // if the thread limit hasn't been reached, a new thread will be spawned to execute
        // the task even if there is an existing idle thread in the pool. This is faster than
        // handing the task to an existing idle thread via the queue. Once the thread limit is
        // reached, subsequent tasks will be queued. If the queue is full, tasks will be rejected.

        super( maximumPoolSize, maximumPoolSize, keepAliveTime, unit,
               new LinkedBlockingQueue<Runnable>( maximumQueueSize ) );

        this.threadPrefix = threadPrefix;
        this.maximumQueueSize = maximumQueueSize;

        setThreadFactory( new ThreadFactoryBuilder().setDaemon( true )
                                                 .setNameFormat( threadPrefix + "-%d" ).build() );

        if( keepAliveTime > 0 ) {
            // Need to specifically configure core threads to timeout.
            allowCoreThreadTimeOut( true );
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getClass().getSimpleName() ).append( " - " + threadPrefix + ":" );
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
