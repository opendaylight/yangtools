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
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

public abstract class CodecEntryObject<T extends EntryObject<T, K>, K extends Key<T>>
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
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile K key;

    protected CodecEntryObject(CommonDataObjectCodecContext<T, ?> context, DataContainerNode data) {
        super(context, data);
    }

    @Override
    public final K key() {
        final Object cached = KEY.getAcquire(this);
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

        final Object obj = listContext.deserialize(mapEntry.name());
        // key is known to be non-null, no need to mask it
        final Object witness = KEY.compareAndExchangeRelease(this, null, obj);
        return witness == null ? obj : witness;
    }
}
