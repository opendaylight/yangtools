/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 * A ThreadPoolExecutor with a specified bounded queue capacity that favors reusing previously
 * constructed threads, when they are available, over creating new threads.
 *
 * <p>See {@link SpecialExecutors#newBoundedCachedThreadPool} for more details.
 *
 * @author Thomas Pantelis
 */
public class CachedThreadPoolExecutor extends ThreadPoolExecutor {

    private static final long IDLE_TIMEOUT_IN_SEC = 60L;

    private final ExecutorQueue executorQueue;

    private final String threadPrefix;

    private final int maximumQueueSize;

    private final RejectedTaskHandler rejectedTaskHandler;

    /**
     * Constructs an instance.
     *
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool. Threads will terminate after
     *            being idle for 60 seconds.
     * @param maximumQueueSize
     *            the capacity of the queue.
     * @param threadPrefix
     *            the name prefix for threads created by this executor.
     * @param loggerIdentity
     *               the class to use as logger name for logging uncaught exceptions from the threads.
     */
    // due to loggerIdentity argument usage
    @SuppressWarnings("checkstyle:LoggerFactoryClassParameter")
    public CachedThreadPoolExecutor(final int maximumPoolSize, final int maximumQueueSize, final String threadPrefix,
            final Class<?> loggerIdentity) {
        // We're using a custom SynchronousQueue that has a backing bounded LinkedBlockingQueue.
        // We don't specify any core threads (first parameter) so, when a task is submitted,
        // the base class will always try to offer to the queue. If there is an existing waiting
        // thread, the offer will succeed and the task will be handed to the thread to execute. If
        // there's no waiting thread, either because there are no threads in the pool or all threads
        // are busy, the base class will try to create a new thread. If the maximum thread limit has
        // been reached, the task will be rejected. We specify a RejectedTaskHandler that tries
        // to offer to the backing queue. If that succeeds, the task will execute as soon as a
        // thread becomes available. If the offer fails to the backing queue, the task is rejected.
        super(0, maximumPoolSize, IDLE_TIMEOUT_IN_SEC, TimeUnit.SECONDS,
               new ExecutorQueue(maximumQueueSize));

        this.threadPrefix = requireNonNull(threadPrefix);
        this.maximumQueueSize = maximumQueueSize;

        setThreadFactory(ThreadFactoryProvider.builder().namePrefix(threadPrefix)
                .logger(LoggerFactory.getLogger(loggerIdentity)).build().get());

        executorQueue = (ExecutorQueue)super.getQueue();

        rejectedTaskHandler = new RejectedTaskHandler(
                executorQueue.getBackingQueue(), CountingRejectedExecutionHandler.newAbortPolicy());
        super.setRejectedExecutionHandler(rejectedTaskHandler);
    }

    @Override
    public void setRejectedExecutionHandler(final RejectedExecutionHandler handler) {
        rejectedTaskHandler.setDelegateRejectedExecutionHandler(requireNonNull(handler));
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedTaskHandler.getDelegateRejectedExecutionHandler();
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return executorQueue.getBackingQueue();
    }

    public long getLargestQueueSize() {
        return executorQueue.getBackingQueue().getLargestQueueSize();
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
                .add("Current Queue Size", executorQueue.getBackingQueue().size())
                .add("Largest Queue Size", getLargestQueueSize())
                .add("Max Queue Size", maximumQueueSize)
                .add("Active Thread Count", getActiveCount())
                .add("Completed Task Count", getCompletedTaskCount())
                .add("Total Task Count", getTaskCount())).toString();
    }

    /**
     * A customized SynchronousQueue that has a backing bounded LinkedBlockingQueue. This class
     * overrides the #poll methods to first try to poll the backing queue for a task. If the backing
     * queue is empty, it calls the base SynchronousQueue#poll method. In this manner, we get the
     * thread reuse behavior of the SynchronousQueue with the added ability to queue tasks when all
     * threads are busy.
     */
    private static class ExecutorQueue extends SynchronousQueue<Runnable> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private static final long POLL_WAIT_TIME_IN_MS = 300;

        @SuppressFBWarnings("SE_BAD_FIELD")
        // Runnable is not Serializable
        private final TrackingLinkedBlockingQueue<Runnable> backingQueue;

        ExecutorQueue(final int maxBackingQueueSize) {
            backingQueue = new TrackingLinkedBlockingQueue<>(maxBackingQueueSize);
        }

        TrackingLinkedBlockingQueue<Runnable> getBackingQueue() {
            return backingQueue;
        }

        @Override
        public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            long totalWaitTime = unit.toMillis(timeout);
            long waitTime = Math.min(totalWaitTime, POLL_WAIT_TIME_IN_MS);
            Runnable task = null;

            // We loop here, each time polling the backingQueue first then our queue, instead of
            // polling each just once. This is to handle the following timing edge case:
            //
            //   We poll the backingQueue and it's empty but, before the call to super.poll,
            //   a task is offered but no thread is immediately available and the task is put on the
            //   backingQueue. There is a slight chance that all the other threads could be at the
            //   same point, in which case they would all call super.poll and wait. If we only
            //   called poll once, no thread would execute the task (unless/until another task was
            //   later submitted). But by looping and breaking the specified timeout into small
            //   periods, one thread will eventually wake up and get the task from the backingQueue
            //   and execute it, although slightly delayed.

            while (task == null) {
                // First try to get a task from the backing queue.
                task = backingQueue.poll();
                if (task == null) {
                    // No task in backing - call the base class to wait for one to be offered.
                    task = super.poll(waitTime, TimeUnit.MILLISECONDS);

                    totalWaitTime -= POLL_WAIT_TIME_IN_MS;
                    if (totalWaitTime <= 0) {
                        break;
                    }

                    waitTime = Math.min(totalWaitTime, POLL_WAIT_TIME_IN_MS);
                }
            }

            return task;
        }

        @Override
        public Runnable poll() {
            Runnable task = backingQueue.poll();
            return task != null ? task : super.poll();
        }
    }

    /**
     * Internal RejectedExecutionHandler that tries to offer rejected tasks to the backing queue.
     * If the queue is full, we throw a RejectedExecutionException by default. The client can
     * override this behavior be specifying their own RejectedExecutionHandler, in which case we
     * delegate to that handler.
     */
    private static class RejectedTaskHandler implements RejectedExecutionHandler {

        private final LinkedBlockingQueue<Runnable> backingQueue;
        private volatile RejectedExecutionHandler delegateRejectedExecutionHandler;

        RejectedTaskHandler(final LinkedBlockingQueue<Runnable> backingQueue,
                             final RejectedExecutionHandler delegateRejectedExecutionHandler) {
            this.backingQueue = backingQueue;
            this.delegateRejectedExecutionHandler = delegateRejectedExecutionHandler;
        }

        void setDelegateRejectedExecutionHandler(
                final RejectedExecutionHandler delegateRejectedExecutionHandler) {
            this.delegateRejectedExecutionHandler = delegateRejectedExecutionHandler;
        }

        RejectedExecutionHandler getDelegateRejectedExecutionHandler() {
            return delegateRejectedExecutionHandler;
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("Executor has been shutdown.");
            }

            if (!backingQueue.offer(task)) {
                delegateRejectedExecutionHandler.rejectedExecution(task, executor);
            }
        }
    }
}
