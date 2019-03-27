/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for QueuedNotificationManager.
 *
 * @author Thomas Pantelis
 */
public class QueuedNotificationManagerTest {

    static class TestListener<N> {

        private final List<N> actual;
        private volatile int expCount;
        private volatile CountDownLatch latch;
        volatile long sleepTime = 0;
        volatile RuntimeException runtimeEx;
        volatile Error jvmError;
        boolean cacheNotifications = true;
        String name;

        TestListener(final int expCount, final int id) {
            name = "TestListener " + id;
            actual = Collections.synchronizedList(new ArrayList<>(expCount));
            reset(expCount);
        }

        void reset(final int newExpCount) {
            this.expCount = newExpCount;
            latch = new CountDownLatch(newExpCount);
            actual.clear();
        }

        void onNotification(final Collection<? extends N> data) {

            try {
                if (sleepTime > 0) {
                    Uninterruptibles.sleepUninterruptibly(sleepTime, TimeUnit.MILLISECONDS);
                }

                if (cacheNotifications) {
                    actual.addAll(data);
                }

                RuntimeException localRuntimeEx = runtimeEx;
                if (localRuntimeEx != null) {
                    runtimeEx = null;
                    throw localRuntimeEx;
                }

                Error localJvmError = jvmError;
                if (localJvmError != null) {
                    jvmError = null;
                    throw localJvmError;
                }

            } finally {
                data.forEach(action -> latch.countDown());
            }
        }

        void verifyNotifications() {
            boolean done = Uninterruptibles.awaitUninterruptibly(latch, 5, TimeUnit.SECONDS);
            if (!done) {
                long actualCount = latch.getCount();
                fail(name + ": Received " + (expCount - actualCount) + " notifications. Expected " + expCount);
            }
        }

        void verifyNotifications(final List<N> expected) {
            verifyNotifications();
            assertEquals(name + ": Notifications", expected, actual);
        }

        // Implement bad hashCode/equals methods to verify it doesn't screw up the
        // QueuedNotificationManager as it should use reference identity.
        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(final Object obj) {
            TestListener<?> other = (TestListener<?>) obj;
            return other != null;
        }
    }

    static class TestListener2<N> extends TestListener<N> {
        TestListener2(final int expCount, final int id) {
            super(expCount, id);
        }
    }

    static class TestListener3<N> extends TestListener<N> {
        TestListener3(final int expCount, final int id) {
            super(expCount, id);
        }
    }

    static class TestNotifier<N> implements BatchedInvoker<TestListener<N>, N> {
        @Override
        public void invokeListener(final TestListener<N> listener, final ImmutableList<N> notifications) {
            listener.onNotification(notifications);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(QueuedNotificationManagerTest.class);
    private ExecutorService queueExecutor;

    @After
    public void tearDown() {
        if (queueExecutor != null) {
            queueExecutor.shutdownNow();
        }
    }

    @Test(timeout = 10000)
    public void testNotificationsWithSingleListener() {

        queueExecutor = Executors.newFixedThreadPool(2);
        NotificationManager<TestListener<Integer>, Integer> manager = QueuedNotificationManager.create(queueExecutor,
                new TestNotifier<>(), 10, "TestMgr");

        int count = 100;

        TestListener<Integer> listener = new TestListener<>(count, 1);
        listener.sleepTime = 20;

        manager.submitNotifications(listener, Arrays.asList(1, 2));
        manager.submitNotification(listener, 3);
        manager.submitNotifications(listener, Arrays.asList(4, 5));
        manager.submitNotification(listener, 6);

        manager.submitNotifications(null, Collections.emptyList());
        manager.submitNotifications(listener, null);
        manager.submitNotification(listener, null);

        Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);

        listener.sleepTime = 0;

        List<Integer> expNotifications = new ArrayList<>(count);
        expNotifications.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
        int initialCount = 6;
        for (int i = 1; i <= count - initialCount; i++) {
            Integer val = Integer.valueOf(initialCount + i);
            expNotifications.add(val);
            manager.submitNotification(listener, val);
        }

        listener.verifyNotifications(expNotifications);
    }

    @Test
    public void testNotificationsWithMultipleListeners() throws InterruptedException {

        int count = 10;
        queueExecutor = Executors.newFixedThreadPool(count);
        final ExecutorService stagingExecutor = Executors.newFixedThreadPool(count);
        final NotificationManager<TestListener<Integer>, Integer> manager = QueuedNotificationManager.create(
                queueExecutor, new TestNotifier<>(), 5000, "TestMgr");

        final int nNotifications = 100000;

        LOG.info("Testing {} listeners with {} notifications each...", count, nNotifications);

        final Integer[] notifications = new Integer[nNotifications];
        for (int i = 1; i <= nNotifications; i++) {
            notifications[i - 1] = Integer.valueOf(i);
        }

        Stopwatch stopWatch = Stopwatch.createStarted();

        List<TestListener<Integer>> listeners = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            final TestListener<Integer> listener =
                    i == 2 ? new TestListener2<>(nNotifications, i) :
                    i == 3 ? new TestListener3<>(nNotifications, i) :
                            new TestListener<>(nNotifications, i);
            listeners.add(listener);

            final Thread t = new Thread(() -> {
                for (int j = 1; j <= nNotifications; j++) {
                    final Integer n = notifications[j - 1];
                    stagingExecutor.execute(() -> manager.submitNotification(listener, n));
                }
            });

            t.start();
            threads.add(t);

        }

        try {
            for (TestListener<Integer> listener: listeners) {
                listener.verifyNotifications();
                LOG.info("{} succeeded", listener.name);
            }
        } finally {
            stagingExecutor.shutdownNow();
        }

        stopWatch.stop();

        LOG.info("Elapsed time: {}", stopWatch);
        LOG.info("Executor: {}", queueExecutor);

        for (Thread t : threads) {
            t.join();
        }
    }

    @Test(timeout = 10000)
    public void testNotificationsWithListenerRuntimeEx() {

        queueExecutor = Executors.newFixedThreadPool(1);
        NotificationManager<TestListener<Integer>, Integer> manager = QueuedNotificationManager.create(queueExecutor,
            new TestNotifier<>(), 10, "TestMgr");

        TestListener<Integer> listener = new TestListener<>(2, 1);
        final RuntimeException mockedRuntimeException = new RuntimeException("mock");
        listener.runtimeEx = mockedRuntimeException;

        manager.submitNotification(listener, 1);
        manager.submitNotification(listener, 2);

        listener.verifyNotifications();
        List<Runnable> tasks = queueExecutor.shutdownNow();
        assertTrue(tasks.isEmpty());
    }

    @Test(timeout = 10000)
    public void testNotificationsWithListenerJVMError() {

        final CountDownLatch errorCaughtLatch = new CountDownLatch(1);
        queueExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
            @Override
            @SuppressWarnings("checkstyle:illegalCatch")
            public void execute(final Runnable command) {
                super.execute(() -> {
                    try {
                        command.run();
                    } catch (Error e) {
                        errorCaughtLatch.countDown();
                    }
                });
            }
        };

        NotificationManager<TestListener<Integer>, Integer> manager = QueuedNotificationManager.create(queueExecutor,
                new TestNotifier<>(), 10, "TestMgr");

        TestListener<Integer> listener = new TestListener<>(2, 1);
        listener.jvmError = mock(Error.class);

        manager.submitNotification(listener, 1);

        assertTrue("JVM Error caught", Uninterruptibles.awaitUninterruptibly(errorCaughtLatch, 5, TimeUnit.SECONDS));

        manager.submitNotification(listener, 2);

        listener.verifyNotifications();
        List<Runnable> tasks = queueExecutor.shutdownNow();
        assertTrue(tasks.isEmpty());
    }
}
