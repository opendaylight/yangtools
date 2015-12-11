/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ForwardingFuture;
import java.util.concurrent.Executor;

/**
 * An implementation of {@link SettableFuture}, which forwards all methods to a {@link #delegate()}. Individual methods
 * can be overridden to specialize their behavior.
 *
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public abstract class ForwardingSettableFuture<V> extends ForwardingFuture<V> implements SettableFuture<V> {
    @Override
    protected abstract SettableFuture<V> delegate();

    @Override
    public boolean set(final V value) {
        return delegate().set(value);
    }

    @Override
    public boolean setException(final Throwable throwable) {
        return delegate().setException(throwable);
    }

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        delegate().addListener(listener, executor);
    }
}
