/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Stopwatch;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests various ThreadPoolExecutor implementations.
 *
 * @author Thomas Pantelis
 */
public class ThreadPoolExecutorTest {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolExecutorTest.class);

    private ExecutorService executor;

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testFastThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(SpecialExecutors.newBoundedFastThreadPool(50, 100000, "TestPool"), 100000, "TestPool",
            0);
    }

    @Test(expected = RejectedExecutionException.class)
    public void testFastThreadPoolRejectingTask() throws InterruptedException {
        executor = SpecialExecutors.newBoundedFastThreadPool(1, 1, "TestPool");

        for (int i = 0; i < 5; i++) {
            executor.execute(new Task(null, null, null, null, TimeUnit.MICROSECONDS.convert(5, TimeUnit.SECONDS)));
        }
    }

    @Test
    public void testBlockingFastThreadPoolExecution() throws InterruptedException {
        // With a queue capacity of 1, it should block at some point.
        testThreadPoolExecution(SpecialExecutors.newBlockingBoundedFastThreadPool(2, 1, "TestPool"), 1000, null, 10);
    }

    @Test
    public void testCachedThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(SpecialExecutors.newBoundedCachedThreadPool(10, 100000, "TestPool"),
                100000, "TestPool", 0);
    }

    @Test(expected = RejectedExecutionException.class)
    public void testCachedThreadRejectingTask() throws InterruptedException {
        ExecutorService executor = SpecialExecutors.newBoundedCachedThreadPool(1, 1, "TestPool");

        for (int i = 0; i < 5; i++) {
            executor.execute(new Task(null, null, null, null, TimeUnit.MICROSECONDS.convert(5, TimeUnit.SECONDS)));
        }
    }

    @Test
    public void testBlockingCachedThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(SpecialExecutors.newBlockingBoundedCachedThreadPool(2, 1, "TestPool"), 1000, null, 10);
    }

    void testThreadPoolExecution(final ExecutorService executor, final int numTasksToRun, final String expThreadPrefix,
            final long taskDelay) throws InterruptedException {

        this.executor = executor;

        LOG.debug("Testing {} with {} tasks.", executor.getClass().getSimpleName(), numTasksToRun);

        final CountDownLatch tasksRunLatch = new CountDownLatch(numTasksToRun);
        final ConcurrentMap<Thread, AtomicLong> taskCountPerThread = new ConcurrentHashMap<>();
        final AtomicReference<AssertionError> threadError = new AtomicReference<>();

        Stopwatch stopWatch = Stopwatch.createStarted();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < numTasksToRun; i++) {
//                    if (i%100 == 0) {
//                        Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MICROSECONDS);
//                    }

                    executor.execute(new Task(tasksRunLatch, taskCountPerThread, threadError, expThreadPrefix,
                        taskDelay));
                }
            }
        }.start();

        boolean done = tasksRunLatch.await(15, TimeUnit.SECONDS);

        stopWatch.stop();

        if (!done) {
            fail(numTasksToRun - tasksRunLatch.getCount() + " tasks out of " + numTasksToRun + " executed");
        }

        if (threadError.get() != null) {
            throw threadError.get();
        }

        LOG.debug("{} threads used:", taskCountPerThread.size());
        for (Map.Entry<Thread, AtomicLong> e : taskCountPerThread.entrySet()) {
            LOG.debug("  {} - {} tasks", e.getKey().getName(), e.getValue());
        }

        LOG.debug("{}", executor);
        LOG.debug("Elapsed time: {}", stopWatch);
    }

    static class Task implements Runnable {
        final CountDownLatch tasksRunLatch;
        final CountDownLatch blockLatch;
        final ConcurrentMap<Thread, AtomicLong> taskCountPerThread;
        final AtomicReference<AssertionError> threadError;
        final String expThreadPrefix;
        final long delay;

        Task(final CountDownLatch tasksRunLatch, final ConcurrentMap<Thread, AtomicLong> taskCountPerThread,
                final AtomicReference<AssertionError> threadError, final String expThreadPrefix, final long delay) {
            this.tasksRunLatch = tasksRunLatch;
            this.taskCountPerThread = taskCountPerThread;
            this.threadError = threadError;
            this.expThreadPrefix = expThreadPrefix;
            this.delay = delay;
            blockLatch = null;
        }

        Task(final CountDownLatch tasksRunLatch, final CountDownLatch blockLatch) {
            this.tasksRunLatch = tasksRunLatch;
            this.blockLatch = blockLatch;
            this.taskCountPerThread = null;
            this.threadError = null;
            this.expThreadPrefix = null;
            this.delay = 0;
        }

        @Override
        public void run() {
            try {
                try {
                    if (delay > 0) {
                        TimeUnit.MICROSECONDS.sleep(delay);
                    } else if (blockLatch != null) {
                        blockLatch.await();
                    }
                } catch (InterruptedException e) {
                    // Ignored
                }

                if (expThreadPrefix != null) {
                    assertTrue("Thread name starts with " + expThreadPrefix,
                            Thread.currentThread().getName().startsWith(expThreadPrefix));
                }

                if (taskCountPerThread != null) {
                    AtomicLong count = taskCountPerThread.get(Thread.currentThread());
                    if (count == null) {
                        count = new AtomicLong(0);
                        AtomicLong prev = taskCountPerThread.putIfAbsent(Thread.currentThread(), count);
                        if (prev != null) {
                            count = prev;
                        }
                    }

                    count.incrementAndGet();
                }

            } catch (AssertionError e) {
                if (threadError != null) {
                    threadError.set(e);
                }
            } finally {
                if (tasksRunLatch != null) {
                    tasksRunLatch.countDown();
                }
            }
        }
    }
}
