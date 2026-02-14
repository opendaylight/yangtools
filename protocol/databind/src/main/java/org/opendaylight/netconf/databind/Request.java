/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A request which can be completed either {@link #completeWith(Object) successfully} or
 * {@link #failWith(RequestException) unsuccessfully}. Each request has two invariants:
 * <ol>
 *   <li>it has a unique identifier, {@link #uuid()}</li>
 *   <li>it has a {@link #principal()}, potentially unknown, which on behalf of whom the request is being made</li>
 * </ol>
 * A request can be thought of as a {@link FutureCallback} with attached metadata, where it is guaranteed the failure
 * cause reported to {@link FutureCallback#onFailure(Throwable)} is always a {@link RequestException}. It can be adapted
 * via {@link #transform(Function)} to a request of a different result type, similar to what a
 * {@link Futures#transform(ListenableFuture, com.google.common.base.Function, Executor)} would do.
 *
 * <p>Completion is always signalled in the calling thread. Callers of {@link #completeWith(Object)} and
 * {@link #failWith(RequestException)} need to ensure that all side effects of the request have been completed. It
 * is recommended that callers do not perform any further operations and just unwind the stack.
 *
 * @param <R> type of reported result
 */
@NonNullByDefault
public interface Request<R> {
    /**
     * Return the identifier of this request.
     *
     * @return a {@link UUID}
     */
    UUID uuid();

    /**
     * {@return the Principal making this request, {@code null} if unauthenticated}
     */
    @Nullable Principal principal();

    /**
     * Returns a request requesting a result of {@code U}. Completion of returned request will result in this request
     * completing. If the request completes successfully, supplied function will be used to transform the result.
     *
     * @param <I> new result type
     * @param function result mapping function
     * @return a new request
     */
    // FIXME: this needs a better name
    @Beta
    <I> Request<I> transform(Function<I, R> function);

    /**
     * Complete this request with specified result.
     *
     * @param result the result
     */
    void completeWith(R result);

    /**
     * Fail this request with specified {@link RequestException}.
     *
     * @param failure the {@link RequestException}
     */
    void failWith(RequestException failure);
}
