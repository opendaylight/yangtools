/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.concurrent.Executor;

/**
 * An {@link OffloadingSettableFuture}, which does not allow listeners to execute on any thread which was spawned
 * via a specific {@link TrackingThreadFactory}, but rather redirects them to a dedicated executor.
 *
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public final class ThreadOffloadingSettableFuture<V> extends OffloadingSettableFuture<V> {
    private final TrackingThreadFactory factory;
    private final SettableFuture<V> delegate;
    private final Executor executor;

    private ThreadOffloadingSettableFuture(final SettableFuture<V> delegate, final TrackingThreadFactory factory,
        final Executor executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.factory = Preconditions.checkNotNull(factory);
        this.executor = Preconditions.checkNotNull(executor);
    }

    public static <V> SettableFuture<V> create(final SettableFuture<V> delegate, final TrackingThreadFactory factory,
            final Executor executor) {
        if (delegate instanceof ThreadOffloadingSettableFuture) {
            final ThreadOffloadingSettableFuture<V> other = (ThreadOffloadingSettableFuture<V>) delegate;
            if (other.factory.equals(factory) && other.executor.equals(executor)) {
                return other;
            }
        }

        return new ThreadOffloadingSettableFuture<V>(delegate, factory, executor);
    }

    @Override
    protected SettableFuture<V> delegate() {
        return delegate;
    }

    @Override
    protected Optional<Executor> listenerExecutor() {
        return factory.createdCurrentThread() ? Optional.of(executor) : Optional.<Executor>absent();
    }
}
