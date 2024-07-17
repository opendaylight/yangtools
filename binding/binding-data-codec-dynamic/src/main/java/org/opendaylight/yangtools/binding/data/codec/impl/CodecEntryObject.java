/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.VerifyException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * A base class for {@link EntryObject}s, backed by {@link DataObjectCodecContext}. While this class is public, it not
 * part of API surface and is an implementation detail. The only reason for it being public is that it needs to be
 * accessible by code generated at runtime.
 *
 * @param <T> EntryObject type
 * @param <K> Key type
 * @since 16.0.0
 */
public abstract class CodecEntryObject<T extends EntryObject<T, K> & JavaDataContainer<T>, K extends Key<T>>
        extends AugmentableCodecDataObject<T> implements EntryObject<T, K> {
    private static final VarHandle KEY;

    static {
        try {
            KEY = MethodHandles.lookup().findVarHandle(CodecEntryObject.class, "key", Key.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Used via VarHandle
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile @Nullable K key;

    protected CodecEntryObject(final CommonDataObjectCodecContext<T, ?> context, final DataContainerNode data) {
        super(context, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final K key() {
        final var cached = KEY.getAcquire(this);
        return (K) (cached != null ? cached : loadKey());
    }

    // Helper split out of codecKey to aid its inlining
    private @NonNull Object loadKey() {
        if (!(codecData() instanceof MapEntryNode mapEntry)) {
            throw new VerifyException("Unsupported value " + codecData());
        }
        if (!(codecContext() instanceof MapCodecContext<?, ?> listContext)) {
            throw new VerifyException("Unexpected context " + codecContext());
        }

        final var obj = listContext.deserialize(mapEntry.name());
        // key is known to be non-null, no need to mask it
        final var witness = KEY.compareAndExchangeRelease(this, null, obj);
        return witness == null ? obj : witness;
    }
}
