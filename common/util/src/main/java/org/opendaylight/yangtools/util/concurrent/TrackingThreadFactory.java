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
import com.google.common.collect.ForwardingObject;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;

/**
 * A {@link ThreadFactory} which tracks which threads have been created through it. It provides
 * {@link #createdCurrentThread()}
 */
@Beta
public abstract class TrackingThreadFactory extends ForwardingObject implements ThreadFactory {
    private static final Object MARKER_OBJECT = new Object();

    /**
     * Internal marker. This thread-local instance will have have the value set for threads which are instantiated
     * by this factory.
     */
    private final ThreadLocal<Object> marker = new ThreadLocal<>();

    @Override
    @Nonnull protected abstract ThreadFactory delegate();

    @Nonnull public static TrackingThreadFactory create(@Nonnull final ThreadFactory delegate) {
        Preconditions.checkNotNull(delegate);

        if (delegate instanceof TrackingThreadFactory) {
            return (TrackingThreadFactory) delegate;
        }

        return new TrackingThreadFactory() {
            @Override
            protected ThreadFactory delegate() {
                return delegate;
            }
        };
    }

    @Override
    public Thread newThread(final Runnable r) {
        return delegate().newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    marker.set(MARKER_OBJECT);
                    r.run();
                } finally {
                    marker.remove();
                }
            }
        });
    }

    /**
     * Check if this thread factory was the one to (directly or indirectly) to create the current thread of execution.
     *
     * @return True if this thread factory created the thread, false otherwise.
     */
    public boolean createdCurrentThread() {
        return marker.get() != null;
    }
}
