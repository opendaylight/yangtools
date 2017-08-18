/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.util.concurrent.AsyncNotifyingListeningExecutorServiceTest.testListenerCallback;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_CALLABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE_WITH_RESULT;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.concurrent.CommonTestUtils.Invoker;

/**
 * Unit tests for DeadlockDetectingListeningExecutorService.
 *
 * @author Thomas Pantelis
 */
public class DeadlockDetectingListeningExecutorServiceTest {

    interface InitialInvoker {
        void invokeExecutor(ListeningExecutorService executor, Runnable task);
    }

    static final InitialInvoker SUBMIT = ListeningExecutorService::submit;

    static final InitialInvoker EXECUTE = Executor::execute;

    public static class TestDeadlockException extends Exception {
        private static final long serialVersionUID = 1L;

    }

    private static final Supplier<Exception> DEADLOCK_EXECUTOR_SUPPLIER = TestDeadlockException::new;

    DeadlockDetectingListeningExecutorService executor;

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    DeadlockDetectingListeningExecutorService newExecutor() {
        return new DeadlockDetectingListeningExecutorService(Executors.newSingleThreadExecutor(),
                DEADLOCK_EXECUTOR_SUPPLIER);
    }

    @Test
    public void testBlockingSubmitOffExecutor() throws Exception {

        executor = newExecutor();

        // Test submit with Callable.

        ListenableFuture<String> future = executor.submit(() -> "foo");

        assertEquals("Future result", "foo", future.get(5, TimeUnit.SECONDS));

        // Test submit with Runnable.

        executor.submit(() -> { }).get();

        // Test submit with Runnable and value.

        future = executor.submit(() -> { }, "foo");

        assertEquals("Future result", "foo", future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @SuppressWarnings("checkstyle:illegalThrows")
    public void testNonBlockingSubmitOnExecutorThread() throws Throwable {

        executor = newExecutor();

        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_CALLABLE);
        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE);
        testNonBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT);

        testNonBlockingSubmitOnExecutorThread(EXECUTE, SUBMIT_CALLABLE);
    }

    @SuppressWarnings("checkstyle:illegalThrows")
    void testNonBlockingSubmitOnExecutorThread(final InitialInvoker initialInvoker, final Invoker invoker)
            throws Throwable {

        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        final CountDownLatch futureCompletedLatch = new CountDownLatch(1);

        Runnable task = () -> Futures.addCallback(invoker.invokeExecutor(executor, null), new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                futureCompletedLatch.countDown();
            }

            @Override
            @SuppressWarnings("checkstyle:parameterName")
            public void onFailure(@Nonnull final Throwable t) {
                caughtEx.set(t);
                futureCompletedLatch.countDown();
            }
        }, MoreExecutors.directExecutor());

        initialInvoker.invokeExecutor(executor, task);

        assertTrue("Task did not complete - executor likely deadlocked",
                futureCompletedLatch.await(5, TimeUnit.SECONDS));

        if (caughtEx.get() != null) {
            throw caughtEx.get();
        }
    }

    @Test
    public void testBlockingSubmitOnExecutorThread() throws InterruptedException {

        executor = newExecutor();

        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_CALLABLE);
        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE);
        testBlockingSubmitOnExecutorThread(SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT);

        testBlockingSubmitOnExecutorThread(EXECUTE, SUBMIT_CALLABLE);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    void testBlockingSubmitOnExecutorThread(final InitialInvoker initialInvoker, final Invoker invoker)
            throws InterruptedException {

        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Runnable task = () -> {

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

        assertTrue("Task did not complete - executor likely deadlocked", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Expected exception thrown", caughtEx.get());
        assertEquals("Caught exception type", TestDeadlockException.class, caughtEx.get().getClass());
    }

    @Test
    public void testListenableFutureCallbackWithExecutor() throws InterruptedException {

        String listenerThreadPrefix = "ListenerThread";
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
