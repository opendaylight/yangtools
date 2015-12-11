/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.AbstractFuture;

/**
 * Default implementation of the {@link SettableFuture} interface. Backed by Guava's {@link AbstractFuture}.
 *
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public final class DefaultSettableFuture<V> extends AbstractFuture<V> implements SettableFuture<V> {
    private DefaultSettableFuture() {
        // Hidden on purpose
    }

    public static <V> SettableFuture<V> create() {
        return new DefaultSettableFuture<>();
    }

    @Override
    public boolean set(final V value) {
        return super.set(value);
    }

    @Override
    public boolean setException(final Throwable throwable) {
        return super.setException(throwable);
    }
}
