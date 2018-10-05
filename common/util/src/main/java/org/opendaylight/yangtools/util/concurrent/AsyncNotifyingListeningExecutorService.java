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
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link com.google.common.util.concurrent.ListeningExecutorService} implementation that also allows
 * for an {@link Executor} to be specified on construction that is used to execute {@link ListenableFuture} callback
 * Runnables, registered via {@link com.google.common.util.concurrent.Futures#addCallback} or
 * {@link ListenableFuture#addListener} directly, asynchronously when a task that is run on this executor completes.
 * This is useful when you want to guarantee listener callback executions are off-loaded onto another thread to avoid
 * blocking the thread that completed the task, as a common use case is to pass an executor that runs tasks in the same
 * thread as the caller (i.e. {@code MoreExecutors#sameThreadExecutor}) to {@link ListenableFuture#addListener}.
 *
 * <p>
 * Most commonly, this class would be used in lieu of {@code MoreExecutors#listeningDecorator} when the underlying
 * delegate Executor is single-threaded, in which case, you may not want ListenableFuture callbacks to block the single
 * thread.
 *
 * <p>
 * Note: the Executor specified on construction does not replace the Executor specified
 * in {@link ListenableFuture#addListener}. The latter Executor is still used however, if it is detected that
 * the listener Runnable would execute in the thread that completed the task, the listener is executed on Executor
 * specified on construction.
 *
 * @author Thomas Pantelis
 * @see AsyncNotifyingListenableFutureTask
 */
public class AsyncNotifyingListeningExecutorService extends AbstractListeningExecutorService {
    private final @NonNull ExecutorService delegate;
    private final @Nullable Executor listenableFutureExecutor;

    /**
     * Constructor.
     *
     * @param delegate the back-end ExecutorService.
     * @param listenableFutureExecutor the executor used to run listener callbacks asynchronously.
     *     If null, no executor is used.
     */
    public AsyncNotifyingListeningExecutorService(final @NonNull ExecutorService delegate,
            @Nullable final Executor listenableFutureExecutor) {
        this.delegate = requireNonNull(delegate);
        this.listenableFutureExecutor = listenableFutureExecutor;
    }

    /**
     * Creates an {@link AsyncNotifyingListenableFutureTask} instance with the listener Executor.
     *
     * @param task the Runnable to execute
     */
    private <T> @NonNull AsyncNotifyingListenableFutureTask<T> newFutureTask(final @NonNull Runnable task,
            final T result) {
        return AsyncNotifyingListenableFutureTask.create(task, result, listenableFutureExecutor);
    }

    /**
     * Returns the delegate ExecutorService.
     */
    protected @NonNull ExecutorService getDelegate() {
        return delegate;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public void execute(final Runnable command) {
        delegate.execute(command);
    }

    @Override
    public <T> ListenableFuture<T> submit(final Callable<T> task) {
        final AsyncNotifyingListenableFutureTask<T> futureTask = AsyncNotifyingListenableFutureTask.create(
            requireNonNull(task), listenableFutureExecutor);
        delegate.execute(futureTask);
        return futureTask;
    }

    @Override
    public ListenableFuture<?> submit(final Runnable task) {
        final AsyncNotifyingListenableFutureTask<Void> futureTask = newFutureTask(requireNonNull(task), null);
        delegate.execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> ListenableFuture<T> submit(final Runnable task, final T result) {
        final AsyncNotifyingListenableFutureTask<T> futureTask = newFutureTask(requireNonNull(task), result);
        delegate.execute(futureTask);
        return futureTask;
    }

    protected @NonNull ToStringHelper addToStringAttributes(final @NonNull ToStringHelper toStringHelper) {
        return toStringHelper;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("delegate", delegate)).toString();
    }
}
