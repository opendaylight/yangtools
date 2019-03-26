/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 * A ThreadPoolExecutor with a specified bounded queue capacity that favors creating new threads
 * over queuing, as the former is faster.
 *
 * <p>See {@link SpecialExecutors#newBoundedFastThreadPool} for more details.
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
     * @param loggerIdentity
     *               the class to use as logger name for logging uncaught exceptions from the threads.
     */
    public FastThreadPoolExecutor(final int maximumPoolSize, final int maximumQueueSize, final String threadPrefix,
            final Class<?> loggerIdentity) {
        this(maximumPoolSize, maximumQueueSize, DEFAULT_IDLE_TIMEOUT_IN_SEC, TimeUnit.SECONDS,
              threadPrefix, loggerIdentity);
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
     * @param loggerIdentity
     *               the class to use as logger name for logging uncaught exceptions from the threads.
     */
    // due to loggerIdentity argument usage
    @SuppressWarnings("checkstyle:LoggerFactoryClassParameter")
    public FastThreadPoolExecutor(final int maximumPoolSize, final int maximumQueueSize, final long keepAliveTime,
            final TimeUnit unit, final String threadPrefix, final Class<?> loggerIdentity) {
        // We use all core threads (the first 2 parameters below equal) so, when a task is submitted,
        // if the thread limit hasn't been reached, a new thread will be spawned to execute
        // the task even if there is an existing idle thread in the pool. This is faster than
        // handing the task to an existing idle thread via the queue. Once the thread limit is
        // reached, subsequent tasks will be queued. If the queue is full, tasks will be rejected.

        super(maximumPoolSize, maximumPoolSize, keepAliveTime, unit,
                new TrackingLinkedBlockingQueue<>(maximumQueueSize));

        this.threadPrefix = threadPrefix;
        this.maximumQueueSize = maximumQueueSize;

        setThreadFactory(ThreadFactoryProvider.builder().namePrefix(threadPrefix)
                .logger(LoggerFactory.getLogger(loggerIdentity)).build().get());

        if (keepAliveTime > 0) {
            // Need to specifically configure core threads to timeout.
            allowCoreThreadTimeOut(true);
        }

        setRejectedExecutionHandler(CountingRejectedExecutionHandler.newAbortPolicy());
    }

    public long getLargestQueueSize() {
        return ((TrackingLinkedBlockingQueue<?>)getQueue()).getLargestQueueSize();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)
                .add("Thread Prefix", threadPrefix)
                .add("Current Thread Pool Size", getPoolSize())
                .add("Largest Thread Pool Size", getLargestPoolSize())
                .add("Max Thread Pool Size", getMaximumPoolSize())
                .add("Current Queue Size", getQueue().size())
                .add("Largest Queue Size", getLargestQueueSize())
                .add("Max Queue Size", maximumQueueSize)
                .add("Active Thread Count", getActiveCount())
                .add("Completed Task Count", getCompletedTaskCount())
                .add("Total Task Count", getTaskCount())).toString();
    }
}
