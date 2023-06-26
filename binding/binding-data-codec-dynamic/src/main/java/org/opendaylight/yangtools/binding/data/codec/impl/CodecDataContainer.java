/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainer;

/**
 * Base class for {@link DataContainer}-backed codec-generated implementations.
 */
abstract sealed class CodecDataContainer<N extends DataContainer, C extends AnalyzedDataContainerCodecContext<?, ?, ?>>
        permits CodecDataObject, CodecYangData {
    // An object representing a null value in a member field.
    private static final @NonNull Object NULL_VALUE = new Object();

    private static final VarHandle CACHED_HASH_CODE;

    static {
        try {
            CACHED_HASH_CODE = MethodHandles.lookup()
                .findVarHandle(CodecDataContainer.class, "cachedHashcode", Integer.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull C context;
    private final @NonNull N data;

    // Accessed via a VarHandle
    @SuppressWarnings("unused")
    // FIXME: consider using a primitive int-based cache (with 0 being uninit)
    private volatile Integer cachedHashcode;

    CodecDataContainer(final C context, final N data) {
        this.context = requireNonNull(context);
        this.data = requireNonNull(data);
    }

    final @NonNull C codecContext() {
        return context;
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

    protected final Object codecMember(final VarHandle handle, final String localName) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, context.getLeafChild(localName));
    }

    protected final Object codecMember(final VarHandle handle, final Class<? extends DataObject> bindingClass) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, context.getStreamChild(bindingClass));
    }

    protected final Object codecMember(final VarHandle handle, final CodecContextSupplier supplier) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, supplier.getCodecContext());
    }

    protected final @NonNull Object codecMemberOrEmpty(final @Nullable Object value,
            final @NonNull Class<? extends DataObject> bindingClass) {
        return value != null ? value : emptyObject(bindingClass);
    }

    // Helper split out of codecMember to aid its inlining
    private Object loadMember(final VarHandle handle, final CodecContext childCtx) {
        final var child = codecData().childByArg(childCtx.getDomPathArgument());

        // We do not want to use Optional.map() here because we do not want to invoke defaultObject() when we have
        // normal value because defaultObject() may end up throwing an exception intentionally.
        final Object obj = child != null ? childCtx.deserializeObject(child) : childCtx.defaultObject();
        final Object witness = handle.compareAndExchangeRelease(this, null, maskNull(obj));
        return witness == null ? obj : unmaskNull(witness);
    }

    private @NonNull Object emptyObject(final @NonNull Class<? extends DataObject> bindingClass) {
        final var childContext = context.getStreamChild(bindingClass);
        if (childContext instanceof StructuralContainerCodecContext<?> structural) {
            return structural.emptyObject();
        }
        throw new VerifyException("Unexpected context " + childContext);
    }

    private static @NonNull Object maskNull(final @Nullable Object unmasked) {
        return unmasked == null ? NULL_VALUE : unmasked;
    }

    private static @Nullable Object unmaskNull(final Object masked) {
        return masked == NULL_VALUE ? null : masked;
    }
}
