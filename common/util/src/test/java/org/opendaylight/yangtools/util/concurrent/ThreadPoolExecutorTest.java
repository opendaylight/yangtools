/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests various ThreadPoolExecutor implementations.
 *
 * @author Thomas Pantelis
 */
class ThreadPoolExecutorTest {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolExecutorTest.class);

    private ExecutorService executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void testFastThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(
                SpecialExecutors.newBoundedFastThreadPool(50, 100000, "TestPool", getClass()), 100000, "TestPool", 0);
    }

    @Test
    void testFastThreadPoolRejectingTask() throws InterruptedException {
        assertThrows(RejectedExecutionException.class, () -> {
            executor = SpecialExecutors.newBoundedFastThreadPool(1, 1, "TestPool", getClass());

            for (int i = 0; i < 5; i++) {
                executor.execute(new Task(null, null, null, null, TimeUnit.MICROSECONDS.convert(5, TimeUnit.SECONDS)));
            }
        });
    }

    @Test
    void testBlockingFastThreadPoolExecution() throws InterruptedException {
        // With a queue capacity of 1, it should block at some point.
        testThreadPoolExecution(
                SpecialExecutors.newBlockingBoundedFastThreadPool(2, 1, "TestPool", getClass()), 1000, null, 10);
    }

    @Test
    void testCachedThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(SpecialExecutors.newBoundedCachedThreadPool(10, 100000, "TestPool", getClass()),
                100000, "TestPool", 0);
    }

    @Test
    void testCachedThreadRejectingTask() throws InterruptedException {
        assertThrows(RejectedExecutionException.class, () -> {
            ExecutorService localExecutor = SpecialExecutors.newBoundedCachedThreadPool(1, 1, "TestPool", getClass());

            for (int i = 0; i < 5; i++) {
                localExecutor.execute(new Task(null, null, null, null, TimeUnit.MICROSECONDS.convert(5,
                        TimeUnit.SECONDS)));
            }
        });
    }

    @Test
    void testBlockingCachedThreadPoolExecution() throws InterruptedException {
        testThreadPoolExecution(
                SpecialExecutors.newBlockingBoundedCachedThreadPool(2, 1, "TestPool", getClass()), 1000, null, 10);
    }

    void testThreadPoolExecution(final ExecutorService executorToTest, final int numTasksToRun,
            final String expThreadPrefix, final long taskDelay) throws InterruptedException {

        this.executor = executorToTest;

        LOG.debug("Testing {} with {} tasks.", executorToTest.getClass().getSimpleName(), numTasksToRun);

        final var tasksRunLatch = new CountDownLatch(numTasksToRun);
        final var taskCountPerThread = new ConcurrentHashMap<Thread, AtomicLong>();
        final var threadError = new AtomicReference<AssertionError>();

        final var stopWatch = Stopwatch.createStarted();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < numTasksToRun; i++) {
//                    if (i%100 == 0) {
//                        Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MICROSECONDS);
//                    }

                    executorToTest.execute(new Task(tasksRunLatch, taskCountPerThread, threadError, expThreadPrefix,
                        taskDelay));
                }
            }
        }.start();

        final var done = tasksRunLatch.await(15, TimeUnit.SECONDS);

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

        LOG.debug("{}", executorToTest);
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
                    assertTrue(Thread.currentThread().getName().startsWith(expThreadPrefix),
                            "Thread name starts with " + expThreadPrefix);
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
