/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract base class for lazily-instantiated {@link CodecContextSupplier} instances.
 *
 * @param <C> {@link CodecContext} type
 */
abstract sealed class LazyCodecContextSupplier<C extends CodecContext> implements CodecContextSupplier
        // Note: while we could merge this class into DataContainerCodecPrototype, we want to keep the lazy-loading part
        //       separate in case we need to non-DataContainer contexts.
        permits DataContainerPrototype {
    private static final VarHandle INSTANCE;

    static {
        try {
            INSTANCE = MethodHandles.lookup().findVarHandle(LazyCodecContextSupplier.class, "instance",
                CodecContext.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Accessed via INSTANCE
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile C instance;

    @Override
    public final C getCodecContext() {
        final var existing = (C) INSTANCE.getAcquire(this);
        return existing != null ? existing : loadInstance();
    }

    private @NonNull C loadInstance() {
        final var tmp = createInstance();
        final var witness = (C) INSTANCE.compareAndExchangeRelease(this, null, tmp);
        return witness == null ? tmp : witness;
    }

    // This method must allow concurrent loading, i.e. nothing in it may have effects outside of the loaded object
    abstract @NonNull C createInstance();
}
