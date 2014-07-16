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
 * This class uses only core pool threads and allows the core threads to terminate when idle after
 * 15 seconds. Since the ThreadPoolExecutor favors creating new threads over queuing (the former is
 * faster) when the current pool size is less than the maximum number of core threads, threads will
 * only be reused when the core thread limit is exceeded and tasks are queued. For example, if the maximum number of
 * threads is 100 and 100 short-lived tasks are submitted within say 10 seconds, then 100 threads
 * will be created and used - existing idle threads will not be reused. This provides the fastest
 * execution of the 100 tasks at the expense of memory and thread resource overhead. Therefore it is
 * advisable to specify a relatively small thread limit (probably no more than 50).
 *
 * @author Thomas Pantelis
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

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
        super( 0, maximumPoolSize, 15L, TimeUnit.SECONDS,
               ExecutorServiceUtil.offerFailingBlockingQueue(
                                new LinkedBlockingQueue<Runnable>( maximumQueueSize ) ) );

        this.threadPrefix = threadPrefix;
        this.maximumQueueSize = maximumQueueSize;

        setThreadFactory( new ThreadFactoryBuilder().setDaemon( true )
                                                 .setNameFormat( threadPrefix + "-%d" ).build() );

        allowCoreThreadTimeOut( true );

        super.setRejectedExecutionHandler( ExecutorServiceUtil.waitInQueueExecutionHandler() );
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
