/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link FutureTask} that also implements the {@link ListenableFuture} interface similar to
 * guava's {@link ListenableFutureTask}. This class differs from ListenableFutureTask in that it
 * allows an {@link Executor} to be specified on construction that is used to execute listener
 * callback Runnables, registered via {@link #addListener}, asynchronously when this task completes.
 * This is useful when you want to guarantee listener executions are off-loaded onto another thread
 * to avoid blocking the thread that completed this task, as a common use case is to pass an
 * executor that runs tasks in the same thread as the caller (ie MoreExecutors#sameThreadExecutor)
 * to {@link #addListener}.
 * <p>
 * Note: the Executor specified on construction does not replace the Executor specified in
 * {@link #addListener}. The latter Executor is still used however, if it is detected that the
 * listener Runnable would execute in the thread that completed this task, the listener
 * is executed on Executor specified on construction.
 *
 * @author Thomas Pantelis
 *
 * @param <V> the Future result value type
 */
public class AsyncNotifyingListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V> {

    /**
     * ThreadLocal used to detect if the task completion thread is running the listeners.
     */
    private static final ThreadLocal<Boolean> ON_TASK_COMPLETION_THREAD_TL = new ThreadLocal<>();

    /**
     *  The execution list to hold our listeners.
     */
    private final ExecutionList executionList = new ExecutionList();

    /**
     * The executor used to run listener callbacks.
     */
    private final Executor listenerExecutor;

    private AsyncNotifyingListenableFutureTask( Callable<V> callable, @Nullable Executor listenerExecutor ) {
        super( callable );
        this.listenerExecutor = listenerExecutor;
    }

    private AsyncNotifyingListenableFutureTask( Runnable runnable, @Nullable V result,
            @Nullable Executor listenerExecutor ) {
        super( runnable, result );
        this.listenerExecutor = listenerExecutor;
    }

    /**
     * Creates an {@code AsyncListenableFutureTask} that will upon running, execute the given
     * {@code Callable}.
     *
     * @param callable the callable task
     * @param listenerExecutor the executor used to run listener callbacks asynchronously.
     *                         If null, no executor is used.
     */
    public static <V> AsyncNotifyingListenableFutureTask<V> create( Callable<V> callable,
            @Nullable Executor listenerExecutor ) {
      return new AsyncNotifyingListenableFutureTask<V>( callable, listenerExecutor );
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
    public static <V> AsyncNotifyingListenableFutureTask<V> create( Runnable runnable, @Nullable V result,
            @Nullable Executor listenerExecutor ) {
      return new AsyncNotifyingListenableFutureTask<V>( runnable, result, listenerExecutor );
    }

    @Override
    public void addListener( Runnable listener, Executor executor ) {
        // If a listenerExecutor was specified on construction, wrap the listener Runnable in a
        // DelegatingRunnable. If the specified executor is one that runs tasks in the same thread
        // as the caller submitting the task (eg MoreExecutors#sameThreadExecutor) and the
        // listener is executed from the #done method, then the DelegatingRunnable will detect this
        // via the ThreadLocal and submit the listener Runnable to the listenerExecutor.
        //
        // On the other hand, if this task is already complete, the call to ExecutionList#add below
        // will execute the listener Runnable immediately and, since the ThreadLocal won't be set,
        // the DelegatingRunnable will run the listener Runnable inline.

        executionList.add( listenerExecutor == null ? listener :
            new DelegatingRunnable( listener, listenerExecutor ), executor );
    }

    /**
     * Called by the base class when the future result is set. We invoke our listeners.
     */
    @Override
    protected void done() {
        ON_TASK_COMPLETION_THREAD_TL.set( Boolean.TRUE );
        try {
            executionList.execute();
        } finally {
            ON_TASK_COMPLETION_THREAD_TL.set( null );
        }
    }

    private static class DelegatingRunnable implements Runnable {

        private final Runnable delegate;
        private final Executor executor;

        DelegatingRunnable( Runnable delegate, Executor executor ) {
            this.delegate = delegate;
            this.executor = executor;
        }

        @Override
        public void run() {
            if( ON_TASK_COMPLETION_THREAD_TL.get() == null ) {
                // We're not running on the task completion thread so run the delegate inline.
                delegate.run();
            } else {
                // We're running on the task completion thread so off-load to the executor.
                executor.execute( delegate );
            }
        }
    }
}
