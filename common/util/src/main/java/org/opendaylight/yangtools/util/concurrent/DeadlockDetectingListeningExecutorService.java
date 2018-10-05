/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An implementation of ListeningExecutorService that attempts to detect deadlock scenarios that
 * could occur if clients invoke the returned Future's <code>get</code> methods synchronously.
 *
 * <p>Deadlock scenarios are most apt to occur with a backing single-threaded executor where setting of
 * the Future's result is executed on the single thread. Here's a scenario:
 * <ul>
 * <li>Client code is currently executing in an executor's single thread.</li>
 * <li>The client submits another task to the same executor.</li>
 * <li>The client calls <code>get()</code> synchronously on the returned Future</li>
 * </ul>
 * The second submitted task will never execute since the single thread is currently executing
 * the client code which is blocked waiting for the submitted task to complete. Thus, deadlock has
 * occurred.
 *
 * <p>This class prevents this scenario via the use of a ThreadLocal variable. When a task is invoked,
 * the ThreadLocal is set and, when a task completes, the ThreadLocal is cleared. Futures returned
 * from this class override the <code>get</code> methods to check if the ThreadLocal is set. If it is,
 * an ExecutionException is thrown with a custom cause.
 *
 * <p>Note that the ThreadLocal is not removed automatically, so some state may be left hanging off of
 * threads which have encountered this class. If you need to clean that state up, use
 * {@link #cleanStateForCurrentThread()}.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 */
public class DeadlockDetectingListeningExecutorService extends AsyncNotifyingListeningExecutorService {
    /*
     * We cannot use a static field simply because our API contract allows nesting, which means some
     * tasks may be submitted to underlay and some to overlay service -- and the two cases need to
     * be discerned reliably.
     */
    private final SettableBooleanThreadLocal deadlockDetector = new SettableBooleanThreadLocal();
    private final Supplier<Exception> deadlockExceptionFunction;

    /**
     * Constructor.
     *
     * @param delegate the backing ExecutorService.
     * @param deadlockExceptionSupplier Supplier that returns an Exception instance to set as the
     *             cause of the ExecutionException when a deadlock is detected.
     */
    public DeadlockDetectingListeningExecutorService(final ExecutorService delegate,
            @Nonnull final Supplier<Exception> deadlockExceptionSupplier) {
        this(delegate, deadlockExceptionSupplier, null);
    }

    /**
     * Constructor.
     *
     * @param delegate the backing ExecutorService.
     * @param deadlockExceptionSupplier Supplier that returns an Exception instance to set as the
     *             cause of the ExecutionException when a deadlock is detected.
     * @param listenableFutureExecutor the executor used to run listener callbacks asynchronously.
     *             If null, no executor is used.
     */
    public DeadlockDetectingListeningExecutorService(final ExecutorService delegate,
            @Nonnull final Supplier<Exception> deadlockExceptionSupplier,
            @Nullable final Executor listenableFutureExecutor) {
        super(delegate, listenableFutureExecutor);
        this.deadlockExceptionFunction = requireNonNull(deadlockExceptionSupplier);
    }

    @Override
    public void execute(@Nonnull final Runnable command) {
        getDelegate().execute(wrapRunnable(command));
    }

    @Nonnull
    @Override
    public <T> ListenableFuture<T> submit(final Callable<T> task) {
        return wrapListenableFuture(super.submit(wrapCallable(task)));
    }

    @Nonnull
    @Override
    public ListenableFuture<?> submit(final Runnable task) {
        return wrapListenableFuture(super.submit(wrapRunnable(task)));
    }

    @Nonnull
    @Override
    public <T> ListenableFuture<T> submit(final Runnable task, final T result) {
        return wrapListenableFuture(super.submit(wrapRunnable(task), result));
    }

    /**
     * Remove the state this instance may have attached to the calling thread. If no state
     * was attached this method does nothing.
     */
    public void cleanStateForCurrentThread() {
        deadlockDetector.remove();
    }

    private SettableBoolean primeDetector() {
        final SettableBoolean b = deadlockDetector.get();
        checkState(!b.isSet(), "Detector for {} has already been primed", this);
        b.set();
        return b;
    }

    private Runnable wrapRunnable(final Runnable task) {
        return () -> {
            final SettableBoolean b = primeDetector();
            try {
                task.run();
            } finally {
                b.reset();
            }
        };
    }

    private <T> Callable<T> wrapCallable(final Callable<T> delagate) {
        return () -> {
            final SettableBoolean b = primeDetector();
            try {
                return delagate.call();
            } finally {
                b.reset();
            }
        };
    }

    private <T> ListenableFuture<T> wrapListenableFuture(final ListenableFuture<T> delegate) {
        /*
         * This creates a forwarding Future that overrides calls to get(...) to check, via the
         * ThreadLocal, if the caller is doing a blocking call on a thread from this executor. If
         * so, we detect this as a deadlock and throw an ExecutionException even though it may not
         * be a deadlock if there are more than 1 thread in the pool. Either way, there's bad
         * practice somewhere, either on the client side for doing a blocking call or in the
         * framework's threading model.
         */
        return new ForwardingListenableFuture.SimpleForwardingListenableFuture<T>(delegate) {
            @Override
            public T get() throws InterruptedException, ExecutionException {
                checkDeadLockDetectorTL();
                return super.get();
            }

            @Override
            public T get(final long timeout, @Nonnull final TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                checkDeadLockDetectorTL();
                return super.get(timeout, unit);
            }

            void checkDeadLockDetectorTL() throws ExecutionException {
                if (deadlockDetector.get().isSet()) {
                    throw new ExecutionException("A potential deadlock was detected.",
                            deadlockExceptionFunction.get());
                }
            }
        };
    }
}
