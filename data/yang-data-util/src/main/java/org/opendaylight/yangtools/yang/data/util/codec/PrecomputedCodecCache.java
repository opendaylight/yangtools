/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Pre-computed thread-safe CodecCache. All possible codecs are created upfront at instantiation time, after which they
 * are available for the cost of a constant lookup.
 *
 * <p>
 * Instantiation needs to occur through {@link LazyCodecCache#toPrecomputed()} after the lazy cache has been fully
 * populated.
 *
 * @author Robert Varga
 */
@Beta
public final class PrecomputedCodecCache<T> extends CodecCache<T> {
    private final Map<TypeDefinition<?>, T> simpleCodecs;
    private final Map<SchemaNode, T> complexCodecs;

    PrecomputedCodecCache(final Map<TypeDefinition<?>, T> simpleCodecs, final Map<SchemaNode, T> complexCodecs) {
        this.simpleCodecs = requireNonNull(simpleCodecs);
        this.complexCodecs = requireNonNull(complexCodecs);
    }

    @Override
    <S extends SchemaNode & TypeAware> T lookupComplex(final S schema) {
        final T ret = complexCodecs.get(schema);
        checkArgument(ret != null, "No codec available for schema %s", schema);
        return ret;
    }

    @Override
    T lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.get(type);
    }

    @Override
    <S extends SchemaNode & TypeAware> T getComplex(final S schema, final T codec) {
        throw new IllegalStateException("Uncached codec for " + schema);
    }

    @Override
    T getSimple(final TypeDefinition<?> type, final T codec) {
        throw new IllegalStateException("Uncached codec for " + type);
    }

    public int complexSize() {
        return complexCodecs.size();
    }

    public int simpleSize() {
        return simpleCodecs.size();
    }
}
