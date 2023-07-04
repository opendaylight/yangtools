/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods for working with {@link FluentFuture}s. This class provides methods which should really live in
 * Guava's, for example in {@link Futures}, as the implementations provided by {@link Futures#immediateFuture(Object)}
 * and others already implement {@link FluentFuture} and so getting a FluentFuture is a matter of pure boiler-plate.
 *
 * <p>
 * {@link #immediateBooleanFluentFuture(boolean)}, {@link #immediateBooleanFluentFuture(Boolean)},
 * {@link #immediateFalseFluentFuture()}, {@link #immediateTrueFluentFuture()} and {@link #immediateNullFluentFuture()}
 * provide low-cardinality constants, which are generally useful to reduce allocations.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@SuppressWarnings("null")
public final class FluentFutures {
    private static final FluentFuture<?> CANCELLED_FUTURE = FluentFuture.from(Futures.immediateCancelledFuture());
    private static final FluentFuture<?> NULL_FUTURE = FluentFuture.from(Futures.immediateFuture(null));
    private static final FluentFuture<Boolean> FALSE_FUTURE = FluentFuture.from(Futures.immediateFuture(Boolean.FALSE));
    private static final FluentFuture<Boolean> TRUE_FUTURE = FluentFuture.from(Futures.immediateFuture(Boolean.TRUE));

    private FluentFutures() {

    }

    /**
     * Return a {@link FluentFuture} which is immediately {@link Future#cancel(boolean)}led.
     *
     * @return An immediately-cancelled FluentFuture.
     */
    @SuppressWarnings("unchecked")
    public static <T> FluentFuture<T> immediateCancelledFluentFuture() {
        return (FluentFuture<T>) CANCELLED_FUTURE;
    }

    /**
     * Return a {@link FluentFuture} which is immediately failed, reporting specified failure {@code cause}.
     *
     * @param cause failure cause
     * @return An immediately-failed FluentFuture.
     * @throws NullPointerException if {@code cause} is null
     */
    public static <T> FluentFuture<T> immediateFailedFluentFuture(final Throwable cause) {
        return FluentFuture.from(Futures.immediateFailedFuture(requireNonNull(cause)));
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed, reporting specified {@code result}.
     *
     * @param result result of the future
     * @return An immediately-completed FluentFuture.
     * @throws NullPointerException if {@code result} is null
     */
    public static <T> FluentFuture<T> immediateFluentFuture(final T result) {
        return FluentFuture.from(Futures.immediateFuture(requireNonNull(result)));
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed with a {@code null} result.
     *
     * @return An immediately-completed FluentFuture.
     */
    @SuppressWarnings("unchecked")
    public static <@Nullable T> FluentFuture<T> immediateNullFluentFuture() {
        return (FluentFuture<T>) NULL_FUTURE;
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed, reporting specified {@code result}.
     *
     * @param result boolean result
     * @return An immediately-completed FluentFuture reporting specified {@code result}
     * @throws NullPointerException if {@code result} is null
     */
    public static FluentFuture<Boolean> immediateBooleanFluentFuture(final Boolean result) {
        return immediateBooleanFluentFuture(result.booleanValue());
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed, reporting specified {@code result}.
     *
     * @param result boolean result
     * @return An immediately-completed FluentFuture reporting specified {@code result}
     */
    public static FluentFuture<Boolean> immediateBooleanFluentFuture(final boolean result) {
        return result ? TRUE_FUTURE : FALSE_FUTURE;
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed, reporting specified {@link Boolean#TRUE}.
     *
     * @return An immediately-completed FluentFuture reporting {@link Boolean#TRUE}
     */
    public static FluentFuture<Boolean> immediateTrueFluentFuture() {
        return TRUE_FUTURE;
    }

    /**
     * Return a {@link FluentFuture} which is immediately completed, reporting specified {@link Boolean#TRUE}.
     *
     * @return An immediately-completed FluentFuture reporting {@link Boolean#TRUE}
     */
    public static FluentFuture<Boolean> immediateFalseFluentFuture() {
        return FALSE_FUTURE;
    }

    /**
     * Submit a {@link Callable} to specified {@link Executor} and return a {@link FluentFuture} that completes with the
     * result of the {@link Callable}.
     *
     * @param <T> Callable return type
     * @param callable The Callable to call
     * @param executor The Executor to use
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> FluentFuture<T> submit(final Callable<T> callable, final Executor executor) {
        return FluentFuture.from(Futures.submit(callable, executor));
    }
}
