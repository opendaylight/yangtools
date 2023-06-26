/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * A base class for {@link DataObject}s backed by {@link AbstractDataObjectCodecContext}. While this class is public, it
 * is not part of API surface and is an implementation detail. The only reason for it being public is that it needs to
 * be accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract non-sealed class CodecDataObject<T extends DataObject> extends CodecDataContainer<DataContainerNode>
        implements DataObject {
    // An object representing a null value in a member field.
    private static final @NonNull Object NULL_VALUE = new Object();

    private final @NonNull CommonDataObjectCodecContext<T, ?> context;

    // Accessed via a VarHandle
    // FIXME: consider using a primitive int-based cache (with 0 being uninit)
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Integer cachedHashcode;

    protected CodecDataObject(final CommonDataObjectCodecContext<T, ?> context, final DataContainerNode data) {
        super(data);
        this.context = requireNonNull(context, "Context must not be null");
    }

    final @NonNull CommonDataObjectCodecContext<T, ?> codecContext() {
        return context;
    }

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

    private @NonNull Object emptyObject(final @NonNull Class<? extends DataObject> bindingClass) {
        final var childContext = context.getStreamChild(bindingClass);
        if (childContext instanceof StructuralContainerCodecContext<?> structural) {
            return structural.emptyObject();
        }
        throw new VerifyException("Unexpected context " + childContext);
    }

    protected final @NonNull Object codecKey(final VarHandle handle) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? cached : loadKey(handle);
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

    // Helper split out of codecKey to aid its inlining
    private @NonNull Object loadKey(final VarHandle handle) {
        final var data = codecData();
        if (!(data instanceof MapEntryNode mapEntry)) {
            throw new VerifyException("Unsupported value " + data);
        }
        if (!(context instanceof MapCodecContext<?, ?> listContext)) {
            throw new VerifyException("Unexpected context " + context);
        }

        final Object obj = listContext.deserialize(mapEntry.name());
        // key is known to be non-null, no need to mask it
        final Object witness = handle.compareAndExchangeRelease(this, null, obj);
        return witness == null ? obj : witness;
    }

    private static @NonNull Object maskNull(final @Nullable Object unmasked) {
        return unmasked == null ? NULL_VALUE : unmasked;
    }

    private static @Nullable Object unmaskNull(final Object masked) {
        return masked == NULL_VALUE ? null : masked;
    }
}
