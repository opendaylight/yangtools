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
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

/**
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public abstract class OffloadingSettableFuture<V> extends ForwardingSettableFuture<V> {
    /**
     * Determine whether it is legal to execute listener on the current thread. If it is not, this method must
     * provide an alternative {@link Executor} where the listener should run. If the listener can be executed on this
     * thread, this method should return {@link Optional#absent()}.
     *
     * @return Optional replacement executor. If none is provided, the listener will be run on current thread.
     */
    @Nonnull protected abstract Optional<Executor> listenerExecutor();

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        delegate().addListener(new Runnable() {
            @Override
            public void run() {
                listenerExecutor().or(MoreExecutors.directExecutor()).execute(listener);
            }
        }, executor);
    }
}
