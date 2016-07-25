/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Factory methods for creating {@link ExecutorService} instances with specific configurations.

 * @author Thomas Pantelis
 */
public final class SpecialExecutors {

    private SpecialExecutors() {
    }

    /**
     * Creates an ExecutorService with a specified bounded queue capacity that favors creating new
     * threads over queuing, as the former is faster, so threads will only be reused when the thread
     * limit is exceeded and tasks are queued. If the maximum queue capacity is reached, subsequent
     * tasks will be rejected.
     *
     * <p>For example, if the maximum number of threads is 100 and 100 short-lived tasks are submitted
     * within say 10 seconds, then 100 threads will be created and used - previously constructed
     * idle threads will not be reused. This provides the fastest execution of the 100 tasks at the
     * expense of memory and thread resource overhead. Therefore it is advisable to specify a
     * relatively small thread limit (probably no more than 50).
     *
     * <p>Threads that have not been used for 15 seconds are terminated and removed from the pool.
     * Thus, a pool that remains idle for long enough will not consume any resources.
     *
     * <p>If you need an executor with less memory and thread resource overhead where slower execution
     * time is acceptable, consider using {@link #newBoundedCachedThreadPool }.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 15 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     * @return a new ExecutorService with the specified configuration.
     */
    public static ExecutorService newBoundedFastThreadPool( int maximumPoolSize,
            int maximumQueueSize, String threadPrefix ) {
        return new FastThreadPoolExecutor( maximumPoolSize, maximumQueueSize, threadPrefix );
    }

    /**
     * Creates an ExecutorService similar to {@link #newBoundedFastThreadPool } except that it
     * handles rejected tasks by running them in the same thread as the caller. Therefore if the
     * queue is full, the caller submitting the task will be blocked until the task completes. In
     * this manner, tasks are never rejected.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 15 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     * @return a new ExecutorService with the specified configuration.
     */
    public static ExecutorService newBlockingBoundedFastThreadPool( int maximumPoolSize,
            int maximumQueueSize, String threadPrefix ) {

        FastThreadPoolExecutor executor =
                new FastThreadPoolExecutor( maximumPoolSize, maximumQueueSize, threadPrefix );
        executor.setRejectedExecutionHandler( CountingRejectedExecutionHandler.newCallerRunsPolicy() );
        return executor;
    }

    /**
     * Creates an ExecutorService with a specified bounded queue capacity that favors reusing
     * previously constructed threads, when they are available, over creating new threads. When a
     * task is submitted, if no existing thread is available, a new thread will be created and added
     * to the pool. If there is an existing idle thread available, the task will be handed to that
     * thread to execute. If the specified maximum thread limit is reached, subsequent tasks will be
     * queued and will execute as threads become available. If the maximum queue capacity is
     * reached, subsequent tasks will be rejected.
     *
     * <p>Threads that have not been used for sixty seconds are terminated and removed from the pool.
     * Thus, a pool that remains idle for long enough will not consume any resources.
     *
     * <p>By reusing threads when possible, this executor optimizes for reduced memory and thread
     * resource overhead at the expense of execution time.
     *
     * <p>If you need an executor with faster execution time where increased memory and thread resource
     * overhead is acceptable, consider using {@link #newBoundedFastThreadPool }.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 60 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     * @return a new ExecutorService with the specified configuration.
     */
    public static ExecutorService newBoundedCachedThreadPool( int maximumPoolSize,
            int maximumQueueSize, String threadPrefix ) {
        return new CachedThreadPoolExecutor( maximumPoolSize, maximumQueueSize, threadPrefix );
    }

    /**
     * Creates an ExecutorService similar to {@link #newBoundedCachedThreadPool } except that it
     * handles rejected tasks by running them in the same thread as the caller. Therefore if the
     * queue is full, the caller submitting the task will be blocked until the task completes. In
     * this manner, tasks are never rejected.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 60 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     * @return a new ExecutorService with the specified configuration.
     */
    public static ExecutorService newBlockingBoundedCachedThreadPool( int maximumPoolSize,
            int maximumQueueSize, String threadPrefix ) {

        CachedThreadPoolExecutor executor =
                new CachedThreadPoolExecutor( maximumPoolSize, maximumQueueSize, threadPrefix );
        executor.setRejectedExecutionHandler( CountingRejectedExecutionHandler.newCallerRunsPolicy() );
        return executor;
    }

    /**
     * Creates an ExecutorService that uses a single worker thread operating off a bounded queue
     * with the specified capacity. Tasks are guaranteed to execute sequentially, and no more than
     * one task will be active at any given time. If the maximum queue capacity is reached,
     * subsequent tasks will be rejected.
     *
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for the thread created by this executor.
     * @return a new ExecutorService with the specified configuration.
     */
    public static ExecutorService newBoundedSingleThreadExecutor( int maximumQueueSize,
            String threadPrefix ) {
        return new FastThreadPoolExecutor( 1, maximumQueueSize, Long.MAX_VALUE, TimeUnit.SECONDS,
                threadPrefix );
    }
}
