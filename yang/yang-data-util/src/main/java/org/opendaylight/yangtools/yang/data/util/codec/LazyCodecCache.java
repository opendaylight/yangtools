/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import java.util.IdentityHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;

/**
 * Lazily-populated CodecCache. This is a non-thread-safe factory, which performs caching of codecs. It is most
 * appropriate for one-off encodings of repetitive data.
 *
 * @author Robert Varga
 */
@Beta
public final class LazyCodecCache<T> extends CodecCache<T> {
    private final Map<TypedDataSchemaNode, T> complexCodecs = new IdentityHashMap<>();
    private final Map<TypeDefinition<?>, T> simpleCodecs = new IdentityHashMap<>();

    @Override
    T getComplex(final TypedDataSchemaNode schema, final T codec) {
        return complexCodecs.computeIfAbsent(schema, any -> codec);
    }

    @Override
    T lookupComplex(final TypedDataSchemaNode schema) {
        return complexCodecs.get(schema);
    }

    @Override
    T lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.get(type);
    }

    @Override
    T getSimple(final TypeDefinition<?> type, final T codec) {
        return simpleCodecs.computeIfAbsent(type, any -> codec);
    }

    public PrecomputedCodecCache<T> toPrecomputed() {
        return new PrecomputedCodecCache<>(simpleCodecs, complexCodecs);
    }
}
