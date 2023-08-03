/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link FutureCallback} exposing a {@link Try}.
 *
 * @param <V> Value type
 */
@Beta
@FunctionalInterface
public interface TryCallback<V> extends FutureCallback<V> {
    /**
     * Add a {@link TryCallback} to a {@link ListenableFuture}. This provides a superior API to
     * {@link Futures#addCallback(ListenableFuture, FutureCallback, Executor)}, as {@link TryCallback} offers a guarded
     * API around {@link Try}: users have to check failure conditions before accessing the resulting value.
     *
     * <p>
     * This method provides a safer alternative to
     * {@link Futures#addCallback(ListenableFuture, FutureCallback, Executor)} while imposing minor runtime overhead,
     * which should be discovered easily. Even if not, the additional access safety offered by {@link Try} in and of
     * itself is a justification for its user.
     *
     * @apiNote
     *   Apparent mis-order with {@link Futures#addCallback(ListenableFuture, FutureCallback, Executor)} is quite
     *   intentional and is not subject to change.
     *
     * @param <T> Value type
     * @param executor {@link Executor} to run {@code callback} on
     * @param future {@link ListenableFuture} potentially producing the value
     * @param callback {@link TryCallback} to execute when {@code future} completes
     * @throws NullPointerException if any argument is {@code null}
     */
    static <V> void addCallback(final Executor executor, final ListenableFuture<V> future,
            final TryCallback<V> callback) {
        Futures.addCallback(future, callback, executor);
    }

    void onComplete(@NonNull Try<V> tryResult);

    /**
     * {@inheritDoc}
     *
     * @implSpec Default implementation performs {@code onComplete(Try.of(result))} and should never be overridden.
     */
    @Override
    default void onSuccess(final V result) {
        onComplete(Try.of(result));
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec Default implementation performs {@code onComplete(Try.failed(cause))} and should never be overridden.
     */
    @Override
    default void onFailure(final Throwable cause) {
        onComplete(Try.failed(cause));
    }
}