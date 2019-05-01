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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

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

    private final @NonNull DataObjectCodecContext<T, ?> context;
    @SuppressWarnings("rawtypes")
    private final @NonNull NormalizedNodeContainer data;

    private volatile Integer cachedHashcode = null;

    protected CodecDataObject(final DataObjectCodecContext<T, ?> context, final NormalizedNodeContainer<?, ?, ?> data) {
        this.data = requireNonNull(data, "Data must not be null");
        this.context = requireNonNull(context, "Context must not be null");
    }

    @Override
    public final int hashCode() {
        final Integer cached = cachedHashcode;
        if (cached != null) {
            return cached;
        }

        final int result = codecAugmentedHashCode();
        cachedHashcode = result;
        return result;
    }

    @Override
    @SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "State is examined indirectly enough to confuse SpotBugs")
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        final Class<? extends DataObject> iface = implementedInterface();
        if (!iface.isInstance(obj)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final T other = (T) iface.cast(obj);
        // Note: we do not want to compare NormalizedNode data here, as we may be looking at different instantiations
        //       of the same grouping -- in which case normalized node will not compare as equal.
        return codecAugmentedEquals(other);
    }

    @Override
    public final String toString() {
        return codecAugmentedFillToString(MoreObjects.toStringHelper(implementedInterface()).omitNullValues())
                .toString();
    }

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

    protected final Object codecKey(final VarHandle handle) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? cached : loadKey(handle);
    }

    protected abstract int codecHashCode();

    protected abstract boolean codecEquals(T other);

    protected abstract ToStringHelper codecFillToString(ToStringHelper helper);

    final @NonNull DataObjectCodecContext<T, ?> codecContext() {
        return context;
    }

    @SuppressWarnings("rawtypes")
    final @NonNull NormalizedNodeContainer codecData() {
        return data;
    }

    // Non-final to allow specialization in AugmentableCodecDataObject
    int codecAugmentedHashCode() {
        return codecHashCode();
    }

    // Non-final to allow specialization in AugmentableCodecDataObject
    boolean codecAugmentedEquals(final T other) {
        return codecEquals(other);
    }

    // Non-final to allow specialization in AugmentableCodecDataObject
    ToStringHelper codecAugmentedFillToString(final ToStringHelper helper) {
        return codecFillToString(helper);
    }

    // Helpers split out of codecMember to aid its inlining
    private Object loadMember(final VarHandle handle, final NodeCodecContext childCtx) {
        @SuppressWarnings("unchecked")
        final Optional<NormalizedNode<?, ?>> child = data.getChild(childCtx.getDomPathArgument());

        // We do not want to use Optional.map() here because we do not want to invoke defaultObject() when we have
        // normal value because defaultObject() may end up throwing an exception intentionally.
        final Object obj = child.isPresent() ? childCtx.deserializeObject(child.get()) : childCtx.defaultObject();
        final Object witness = handle.compareAndExchangeRelease(this, null, maskNull(obj));
        return witness == null ? obj : unmaskNull(witness);
    }

    // Helpers split out of codecMember to aid its inlining
    private Object loadKey(final VarHandle handle) {
        verify(data instanceof MapEntryNode, "Unsupported value %s", data);
        verify(context instanceof KeyedListNodeCodecContext, "Unexpected context %s", context);
        final Object obj = ((KeyedListNodeCodecContext<?>) context).deserialize(((MapEntryNode) data).getIdentifier());
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
