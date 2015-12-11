/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;

/**
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public abstract class ThreadProtectingSettableFuture<V> extends ForwardingSettableFuture<V> {

    @Nonnull protected abstract TrackingThreadFactory factory();

    public static <V> SettableFuture<V> create(final SettableFuture<V> delegate, final TrackingThreadFactory factory) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkNotNull(factory);

        return new ThreadProtectingSettableFuture<V>() {
            @Override
            protected TrackingThreadFactory factory() {
                return factory;
            }

            @Override
            protected SettableFuture<V> delegate() {
                return delegate;
            }
        };
    }

    private void checkThread() {
        if (factory().createdCurrentThread()) {
            throw new IllegalThreadStateException("Synchronous wait is not allowed in this thread");
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        checkThread();
        return super.get();
    }

    @Override
    public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
        TimeoutException {
        checkThread();
        return super.get(timeout, unit);
    }
}
