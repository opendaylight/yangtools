/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * A base class for {@link DataObject}s backed by {@link DataObjectCodecContext}. While this class is public, it not
 * part of API surface and is an implementation detail. The only reason for it being public is that it needs to be
 * accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract class CodecDataObject<T extends DataObject> implements DataObject {
    // An object representing a null value in a member field.
    private static final @NonNull Object NULL_VALUE = new Object();

    private static final VarHandle CACHED_HASH_CODE;

    static {
        try {
            CACHED_HASH_CODE = MethodHandles.lookup().findVarHandle(CodecDataObject.class, "cachedHashcode",
                Integer.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull AbstractDataObjectCodecContext<T, ?> context;
    private final @NonNull DataContainerNode data;

    // Accessed via a VarHandle
    @SuppressWarnings("unused")
    // FIXME: consider using a primitive int-based cache (with 0 being uninit)
    private volatile Integer cachedHashcode;

    protected CodecDataObject(final AbstractDataObjectCodecContext<T, ?> context, final DataContainerNode data) {
        this.data = requireNonNull(data, "Data must not be null");
        this.context = requireNonNull(context, "Context must not be null");
    }

    @Override
    public final int hashCode() {
        final var cached = (Integer) CACHED_HASH_CODE.getAcquire(this);
        return cached != null ? cached : loadHashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        // Indirection to keep checkstyle happy
        return codecEquals(obj);
    }

    @Override
    public abstract String toString();

    protected final Object codecMember(final VarHandle handle, final String localName) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, context.getLeafChild(localName));
    }

    protected final Object codecMember(final VarHandle handle, final Class<? extends DataObject> bindingClass) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, context.streamChild(bindingClass));
    }

    protected final Object codecMember(final VarHandle handle, final NodeContextSupplier supplier) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? unmaskNull(cached) : loadMember(handle, supplier.get());
    }

    protected final @NonNull Object codecMemberOrEmpty(final @Nullable Object value,
            final @NonNull Class<? extends DataObject> bindingClass) {
        return value != null ? value : emptyObject(bindingClass);
    }

    private @NonNull Object emptyObject(final @NonNull Class<? extends DataObject> bindingClass) {
        final var childContext = context.streamChild(bindingClass);
        verify(childContext instanceof NonPresenceContainerNodeCodecContext, "Unexpected context %s", childContext);
        return ((NonPresenceContainerNodeCodecContext<?>) childContext).emptyObject();
    }

    protected final @NonNull Object codecKey(final VarHandle handle) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? cached : loadKey(handle);
    }

    protected abstract int codecHashCode();

    protected abstract boolean codecEquals(Object obj);

    final @NonNull AbstractDataObjectCodecContext<T, ?> codecContext() {
        return context;
    }

    final @NonNull DataContainerNode codecData() {
        return data;
    }

    // Helper split out of codecMember to aid its inlining
    private Object loadMember(final VarHandle handle, final NodeCodecContext childCtx) {
        final var child = data.childByArg(childCtx.getDomPathArgument());

        // We do not want to use Optional.map() here because we do not want to invoke defaultObject() when we have
        // normal value because defaultObject() may end up throwing an exception intentionally.
        final Object obj = child != null ? childCtx.deserializeObject(child) : childCtx.defaultObject();
        final Object witness = handle.compareAndExchangeRelease(this, null, maskNull(obj));
        return witness == null ? obj : unmaskNull(witness);
    }

    // Helper split out of codecKey to aid its inlining
    private @NonNull Object loadKey(final VarHandle handle) {
        verify(data instanceof MapEntryNode, "Unsupported value %s", data);
        verify(context instanceof KeyedListNodeCodecContext, "Unexpected context %s", context);
        final Object obj = ((KeyedListNodeCodecContext<?, ?>) context)
                .deserialize(((MapEntryNode) data).name());
        // key is known to be non-null, no need to mask it
        final Object witness = handle.compareAndExchangeRelease(this, null, obj);
        return witness == null ? obj : witness;
    }

    // Helper split out of hashCode() to aid its inlining
    private int loadHashCode() {
        final int result = codecHashCode();
        final Object witness = CACHED_HASH_CODE.compareAndExchangeRelease(this, null, result);
        return witness == null ? result : (Integer) witness;
    }

    private static @NonNull Object maskNull(final @Nullable Object unmasked) {
        return unmasked == null ? NULL_VALUE : unmasked;
    }

    private static @Nullable Object unmaskNull(final Object masked) {
        return masked == NULL_VALUE ? null : masked;
    }
}
