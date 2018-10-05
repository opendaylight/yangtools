/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_CALLABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE;
import static org.opendaylight.yangtools.util.concurrent.CommonTestUtils.SUBMIT_RUNNABLE_WITH_RESULT;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.util.concurrent.CommonTestUtils.Invoker;

/**
 * Unit tests for AsyncNotifyingListeningExecutorService.
 *
 * @author Thomas Pantelis
 */
public class AsyncNotifyingListeningExecutorServiceTest {

    private ExecutorService listenerExecutor;
    private AsyncNotifyingListeningExecutorService testExecutor;

    @After
    public void tearDown() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdownNow();
        }

        if (testExecutor != null) {
            testExecutor.shutdownNow();
        }
    }

    @Test
    public void testListenerCallbackWithExecutor() throws InterruptedException {

        String listenerThreadPrefix = "ListenerThread";
        listenerExecutor = Executors.newFixedThreadPool(3,
                new ThreadFactoryBuilder().setNameFormat(listenerThreadPrefix + "-%d").build());

        testExecutor = new AsyncNotifyingListeningExecutorService(
                Executors.newSingleThreadExecutor(
                        new ThreadFactoryBuilder().setNameFormat("SingleThread").build()),
                listenerExecutor);

        testListenerCallback(testExecutor, SUBMIT_CALLABLE, listenerThreadPrefix);
        testListenerCallback(testExecutor, SUBMIT_RUNNABLE, listenerThreadPrefix);
        testListenerCallback(testExecutor, SUBMIT_RUNNABLE_WITH_RESULT, listenerThreadPrefix);
    }

    @Test
    public void testListenerCallbackWithNoExecutor() throws InterruptedException {

        String listenerThreadPrefix = "SingleThread";
        testExecutor = new AsyncNotifyingListeningExecutorService(
                Executors.newSingleThreadExecutor(
                        new ThreadFactoryBuilder().setNameFormat(listenerThreadPrefix).build()),
                null);

        testListenerCallback(testExecutor, SUBMIT_CALLABLE, listenerThreadPrefix);
        testListenerCallback(testExecutor, SUBMIT_RUNNABLE, listenerThreadPrefix);
        testListenerCallback(testExecutor, SUBMIT_RUNNABLE_WITH_RESULT, listenerThreadPrefix);
    }

    static void testListenerCallback(final AsyncNotifyingListeningExecutorService executor,
            final Invoker invoker, final String expListenerThreadPrefix) throws InterruptedException {

        AtomicReference<AssertionError> assertError = new AtomicReference<>();
        CountDownLatch futureNotifiedLatch = new CountDownLatch(1);
        CountDownLatch blockTaskLatch = new CountDownLatch(1);

        // The blockTaskLatch is used to block the task from completing until we've added
        // our listener to the Future. Otherwise, if the task completes quickly and the Future is
        // set to done before we've added our listener, the call to ListenableFuture#addListener
        // will immediately notify synchronously on this thread as Futures#addCallback defaults to
        // a same thread executor. This would erroneously fail the test.

        ListenableFuture<?> future = invoker.invokeExecutor(executor, blockTaskLatch);
        addCallback(future, futureNotifiedLatch, expListenerThreadPrefix, assertError);

        // Now that we've added our listener, signal the latch to let the task complete.

        blockTaskLatch.countDown();

        assertTrue("ListenableFuture callback was not notified of onSuccess",
                    futureNotifiedLatch.await(5, TimeUnit.SECONDS));

        if (assertError.get() != null) {
            throw assertError.get();
        }

        // Add another listener - since the Future is already complete, we expect the listener to be
        // notified inline on this thread when it's added.

        futureNotifiedLatch = new CountDownLatch(1);
        addCallback(future, futureNotifiedLatch, Thread.currentThread().getName(), assertError);

        assertTrue("ListenableFuture callback was not notified of onSuccess",
                    futureNotifiedLatch.await(5, TimeUnit.SECONDS));

        if (assertError.get() != null) {
            throw assertError.get();
        }
    }

    static void addCallback(final ListenableFuture<?> future, final CountDownLatch futureNotifiedLatch,
            final String expListenerThreadPrefix, final AtomicReference<AssertionError> assertError) {

        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                try {
                    String theadName = Thread.currentThread().getName();
                    assertTrue("ListenableFuture callback was not notified on the listener executor."
                        + " Expected thread name prefix \"" + expListenerThreadPrefix
                        + "\". Actual thread name \"" + theadName + "\"",
                            theadName.startsWith(expListenerThreadPrefix));
                } catch (AssertionError e) {
                    assertError.set(e);
                } finally {
                    futureNotifiedLatch.countDown();
                }
            }

            @Override
            @SuppressWarnings("checkstyle:parameterName")
            public void onFailure(@Nonnull final Throwable t) {
                // Shouldn't happen
                fail("Unexpected failure " + t);
            }
        }, MoreExecutors.directExecutor());
    }

    @Test
    public void testDelegatedMethods() throws InterruptedException {

        Runnable task = () -> { };

        List<Runnable> taskList = new ArrayList<>();

        ExecutorService mockDelegate = mock(ExecutorService.class);
        doNothing().when(mockDelegate).execute(task);
        doNothing().when(mockDelegate).shutdown();
        doReturn(taskList).when(mockDelegate).shutdownNow();
        doReturn(Boolean.TRUE).when(mockDelegate).awaitTermination(3, TimeUnit.SECONDS);
        doReturn(Boolean.TRUE).when(mockDelegate).isShutdown();
        doReturn(Boolean.TRUE).when(mockDelegate).isTerminated();

        AsyncNotifyingListeningExecutorService executor = new AsyncNotifyingListeningExecutorService(
                                                                   mockDelegate, null);

        executor.execute(task);
        executor.shutdown();
        assertTrue("awaitTermination", executor.awaitTermination(3, TimeUnit.SECONDS));
        assertSame("shutdownNow", taskList, executor.shutdownNow());
        assertTrue("isShutdown", executor.isShutdown());
        assertTrue("isTerminated", executor.isTerminated());

        verify(mockDelegate).execute(task);
        verify(mockDelegate).shutdown();
        verify(mockDelegate).awaitTermination(3, TimeUnit.SECONDS);
        verify(mockDelegate).shutdownNow();
        verify(mockDelegate).isShutdown();
        verify(mockDelegate).isTerminated();
    }
}
