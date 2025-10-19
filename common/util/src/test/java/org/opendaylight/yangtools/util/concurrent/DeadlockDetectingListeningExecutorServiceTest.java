/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.util.concurrent.AsyncNotifyingListeningExecutorServiceTest.testListenerCallback;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_CALLABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE_WITH_RESULT;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.concurrent.CommonTestUtils.Invoker;

/**
 * Unit tests for DeadlockDetectingListeningExecutorService.
 *
 * @author Thomas Pantelis
 */
class DeadlockDetectingListeningExecutorServiceTest {

    interface InitialInvoker {
        void invokeExecutor(ListeningExecutorService executor, Runnable task);
    }

    static final InitialInvoker SUBMIT = (executor, task) -> {
        assertNotNull(executor.submit(task));
    };

    static final InitialInvoker EXECUTE = Executor::execute;

    public static class TestDeadlockException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }

    private static final Supplier<Exception> DEADLOCK_EXECUTOR_SUPPLIER = TestDeadlockException::new;

    DeadlockDetectingListeningExecutorService executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    DeadlockDetectingListeningExecutorService newExecutor() {
        return new DeadlockDetectingListeningExecutorService(Executors.newSingleThreadExecutor(),
                DEADLOCK_EXECUTOR_SUPPLIER);
    }

    @Test
    void testBlockingSubmitOffExecutor() throws Exception {

        executor = newExecutor();

        // Test submit with Callable.

        var future = executor.submit(() -> "foo");

        assertEquals("foo", future.get(5, TimeUnit.SECONDS), "Future result");

        // Test submit with Runnable.

        executor.submit(() -> { }).get();

        // Test submit with Runnable and value.

        future = executor.submit(() -> { }, "foo");

        assertEquals("foo", future.get(5, TimeUnit.SECONDS), "Future result");
    }

    @Test
    @SuppressWarnings("checkstyle:illegalThrows")
    void testNonBlockingSubmitOnExecutorThread() throws Throwable {

        executor = newExecutor();

        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_CALLABLE);
        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE);
        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT);

        testNonBlockingSubmitOnExecutorThread(EXECUTE, SUBMIT_CALLABLE);
    }

    @SuppressWarnings("checkstyle:illegalThrows")
    void testNonBlockingSubmitOnExecutorThread(final InitialInvoker initialInvoker, final Invoker invoker)
            throws Throwable {

        final var caughtEx = new AtomicReference<Throwable>();
        final var futureCompletedLatch = new CountDownLatch(1);

        final var task = (Runnable) () -> Futures.addCallback(invoker.invokeExecutor(executor, null),
                new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(final Object result) {
                        futureCompletedLatch.countDown();
                    }

                    @Override
                    @SuppressWarnings("checkstyle:parameterName")
                    public void onFailure(final Throwable t) {
                        caughtEx.set(t);
                        futureCompletedLatch.countDown();
                    }
                }, MoreExecutors.directExecutor());

        initialInvoker.invokeExecutor(executor, task);

        assertTrue(futureCompletedLatch.await(5, TimeUnit.SECONDS),
                "Task did not complete - executor likely deadlocked");

        if (caughtEx.get() != null) {
            throw caughtEx.get();
        }
    }

    @Test
    void testBlockingSubmitOnExecutorThread() throws InterruptedException {

        executor = newExecutor();

        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_CALLABLE);
        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE);
        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT);

        testBlockingSubmitOnExecutorThread(EXECUTE, SUBMIT_CALLABLE);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    void testBlockingSubmitOnExecutorThread(final InitialInvoker initialInvoker, final Invoker invoker)
            throws InterruptedException {

        final var caughtEx = new AtomicReference<Throwable>();
        final var latch = new CountDownLatch(1);

        final var task = (Runnable) () -> {

            try {
                invoker.invokeExecutor(executor, null).get();
            } catch (ExecutionException e) {
                caughtEx.set(e.getCause());
            } catch (Throwable e) {
                caughtEx.set(e);
            } finally {
                latch.countDown();
            }
        };

        initialInvoker.invokeExecutor(executor, task);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Task did not complete - executor likely deadlocked");
        assertNotNull(caughtEx.get(), "Expected exception thrown");
        assertEquals(TestDeadlockException.class, caughtEx.get().getClass(), "Caught exception type");
    }

    @Test
    void testListenableFutureCallbackWithExecutor() throws InterruptedException {

        final var listenerThreadPrefix = "ListenerThread";
        ExecutorService listenerExecutor = Executors.newFixedThreadPool(1,
                new ThreadFactoryBuilder().setNameFormat(listenerThreadPrefix + "-%d").build());

        executor = new DeadlockDetectingListeningExecutorService(
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SingleThread").build()),
                DEADLOCK_EXECUTOR_SUPPLIER, listenerExecutor);

        try {
            testListenerCallback(executor, SUBMIT_CALLABLE, listenerThreadPrefix);
            testListenerCallback(executor, SUBMIT_RUNNABLE, listenerThreadPrefix);
            testListenerCallback(executor, SUBMIT_RUNNABLE_WITH_RESULT, listenerThreadPrefix);
        } finally {
            listenerExecutor.shutdownNow();
        }
    }
}
