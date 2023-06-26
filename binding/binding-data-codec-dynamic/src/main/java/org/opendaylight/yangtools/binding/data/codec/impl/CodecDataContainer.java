/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainer;

/**
 * Base class for {@link DataContainer}-backed codec-generated implementations.
 */
abstract sealed class CodecDataContainer<N extends DataContainer> permits CodecDataObject, CodecYangData {
    private static final VarHandle CACHED_HASH_CODE;

    static {
        try {
            CACHED_HASH_CODE = MethodHandles.lookup().findVarHandle(CodecDataContainer.class, "cachedHashcode",
                Integer.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull N data;

    // Accessed via a VarHandle
    @SuppressWarnings("unused")
    // FIXME: consider using a primitive int-based cache (with 0 being uninit)
    private volatile Integer cachedHashcode;

    CodecDataContainer(final N data) {
        this.data = requireNonNull(data);
    }

    final @NonNull N codecData() {
        return data;
    }

    @Override
    public final int hashCode() {
        final var cached = (Integer) CACHED_HASH_CODE.getAcquire(this);
        return cached != null ? cached : loadHashCode();
    }

    // Helper split out of hashCode() to aid its inlining
    private int loadHashCode() {
        final int result = codecHashCode();
        final Object witness = CACHED_HASH_CODE.compareAndExchangeRelease(this, null, result);
        return witness == null ? result : (Integer) witness;
    }

    protected abstract int codecHashCode();

    @Override
    public final boolean equals(final Object obj) {
        // Indirection to keep checkstyle happy
        return codecEquals(obj);
    }

    protected abstract boolean codecEquals(Object obj);

    @Override
    public abstract String toString();
}
