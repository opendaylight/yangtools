/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.VerifyException;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * A base class for {@link DataObject}s backed by {@link CommonDataObjectCodecContext}. While this class is public, it
 * is not part of API surface and is an implementation detail. The only reason for it being public is that it needs to
 * be accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract non-sealed class CodecDataObject<T extends DataObject>
        extends CodecDataContainer<DataContainerNode, CommonDataObjectCodecContext<T, ?>>
        implements DataObject {
    protected CodecDataObject(final CommonDataObjectCodecContext<T, ?> context, final DataContainerNode data) {
        super(context, data);
    }

    protected final @NonNull Object codecKey(final VarHandle handle) {
        final Object cached = handle.getAcquire(this);
        return cached != null ? cached : loadKey(handle);
    }

    // Helper split out of codecKey to aid its inlining
    private @NonNull Object loadKey(final VarHandle handle) {
        final var data = codecData();
        if (!(data instanceof MapEntryNode mapEntry)) {
            throw new VerifyException("Unsupported value " + data);
        }
        final var context = codecContext();
        if (!(context instanceof MapCodecContext<?, ?> listContext)) {
            throw new VerifyException("Unexpected context " + context);
        }

        final Object obj = listContext.deserialize(mapEntry.name());
        // key is known to be non-null, no need to mask it
        final Object witness = handle.compareAndExchangeRelease(this, null, obj);
        return witness == null ? obj : witness;
    }
}
