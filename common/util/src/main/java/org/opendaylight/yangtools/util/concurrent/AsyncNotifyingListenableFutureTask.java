/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FutureTask} that also implements the {@link ListenableFuture} interface similar to
 * guava's {@link ListenableFutureTask}. This class differs from ListenableFutureTask in that it
 * allows an {@link Executor} to be specified on construction that is used to execute listener
 * callback Runnables, registered via {@link #addListener}, asynchronously when this task completes.
 * This is useful when you want to guarantee listener executions are off-loaded onto another thread
 * to avoid blocking the thread that completed this task, as a common use case is to pass an
 * executor that runs tasks in the same thread as the caller (ie MoreExecutors#sameThreadExecutor)
 * to {@link #addListener}.
 *
 * <p>Note: the Executor specified on construction does not replace the Executor specified in
 * {@link #addListener}. The latter Executor is still used however, if it is detected that the
 * listener Runnable would execute in the thread that completed this task, the listener
 * is executed on Executor specified on construction.
 *
 * <p>Also note that the use of this task may attach some (small) amount of state to the threads
 * interacting with it. That state will not be detached automatically, but you can use
 *  {@link #cleanStateForCurrentThread()} to clean it up.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 *
 * @param <V> the Future result value type
 */
public class AsyncNotifyingListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V> {

    private static final class DelegatingAsyncNotifyingListenableFutureTask<V>
            extends AsyncNotifyingListenableFutureTask<V> {

        /**
         * The executor used to run listener callbacks.
         */
        private final Executor listenerExecutor;

        private DelegatingAsyncNotifyingListenableFutureTask(final Callable<V> callable,
                @Nullable final Executor listenerExecutor) {
            super(callable);
            this.listenerExecutor = requireNonNull(listenerExecutor);
        }

        private DelegatingAsyncNotifyingListenableFutureTask(final Runnable runnable, @Nullable final V result,
                @Nullable final Executor listenerExecutor) {
            super(runnable, result);
            this.listenerExecutor = requireNonNull(listenerExecutor);
        }

        @Override
        public void addListener(@Nonnull final Runnable listener, @Nonnull final Executor executor) {
            // Wrap the listener Runnable in a DelegatingRunnable. If the specified executor is one that
            // runs tasks in the same thread as the caller submitting the task
            // (e.g. {@link com.google.common.util.concurrent.MoreExecutors#sameThreadExecutor}) and the
            // listener is executed from the #done method, then the DelegatingRunnable will detect this
            // via the ThreadLocal and submit the listener Runnable to the listenerExecutor.
            //
            // On the other hand, if this task is already complete, the call to ExecutionList#add in
            // superclass will execute the listener Runnable immediately and, since the ThreadLocal won't be set,
            // the DelegatingRunnable will run the listener Runnable inline.
            super.addListener(new DelegatingRunnable(listener, listenerExecutor), executor);
        }
    }

    private static final class DelegatingRunnable implements Runnable {
        private final Runnable delegate;
        private final Executor executor;

        DelegatingRunnable(final Runnable delegate, final Executor executor) {
            this.delegate = requireNonNull(delegate);
            this.executor = requireNonNull(executor);
        }

        @Override
        public void run() {
            if (ON_TASK_COMPLETION_THREAD_TL.get().isSet()) {
                // We're running on the task completion thread so off-load to the executor.
                LOG.trace("Submitting ListenenableFuture Runnable from thread {} to executor {}",
                        Thread.currentThread().getName(), executor);
                executor.execute(delegate);
            } else {
                // We're not running on the task completion thread so run the delegate inline.
                LOG.trace("Executing ListenenableFuture Runnable on this thread: {}",
                        Thread.currentThread().getName());
                delegate.run();
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AsyncNotifyingListenableFutureTask.class);

    /**
     * ThreadLocal used to detect if the task completion thread is running the listeners.
     */
    private static final SettableBooleanThreadLocal ON_TASK_COMPLETION_THREAD_TL = new SettableBooleanThreadLocal();

    /**
     *  The execution list to hold our listeners.
     */
    private final ExecutionList executionList = new ExecutionList();

    private AsyncNotifyingListenableFutureTask(final Callable<V> callable) {
        super(callable);
    }

    private AsyncNotifyingListenableFutureTask(final Runnable runnable, @Nullable final V result) {
        super(runnable, result);
    }

    /**
     * Creates an {@code AsyncListenableFutureTask} that will upon running, execute the given
     * {@code Callable}.
     *
     * @param callable the callable task
     * @param listenerExecutor the executor used to run listener callbacks asynchronously.
     *                         If null, no executor is used.
     */
    public static <V> AsyncNotifyingListenableFutureTask<V> create(final Callable<V> callable,
            @Nullable final Executor listenerExecutor) {
        if (listenerExecutor == null) {
            return new AsyncNotifyingListenableFutureTask<>(callable);
        }
        return new DelegatingAsyncNotifyingListenableFutureTask<>(callable, listenerExecutor);
    }

    /**
     * Creates a {@code AsyncListenableFutureTask} that will upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion.
     * @param listenerExecutor the executor used to run listener callbacks asynchronously.
     *                         If null, no executor is used.
     */
    public static <V> AsyncNotifyingListenableFutureTask<V> create(final Runnable runnable, @Nullable final V result,
            @Nullable final Executor listenerExecutor) {
        if (listenerExecutor == null) {
            return new AsyncNotifyingListenableFutureTask<>(runnable, result);
        }
        return new DelegatingAsyncNotifyingListenableFutureTask<>(runnable, result, listenerExecutor);
    }

    @Override
    public void addListener(@Nonnull final Runnable listener, @Nonnull final Executor executor) {
        executionList.add(listener, executor);
    }

    /**
     * Remove the state which may have attached to the calling thread. If no state
     * was attached this method does nothing.
     */
    public static void cleanStateForCurrentThread() {
        ON_TASK_COMPLETION_THREAD_TL.remove();
    }

    /**
     * Called by the base class when the future result is set. We invoke our listeners.
     */
    @Override
    protected void done() {
        final SettableBoolean b = ON_TASK_COMPLETION_THREAD_TL.get();
        b.set();

        try {
            executionList.execute();
        } finally {
            b.reset();
        }
    }
}
