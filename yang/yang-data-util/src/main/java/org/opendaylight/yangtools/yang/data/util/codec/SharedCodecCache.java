/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * A thread-safe lazily-populated codec cache. Instances are cached in an internal weak/soft cache.
 *
 * @author Robert Varga
 */
@Beta
public final class SharedCodecCache<T> extends CodecCache<T> {
    // Weak keys to force identity lookup
    // Soft values to keep unreferenced codecs around for a bit, but eventually we want them to go away
    private final Cache<TypeDefinition<?>, T> simpleCodecs = Caffeine.newBuilder().weakKeys().softValues().build();
    private final Cache<SchemaNode, T> complexCodecs = Caffeine.newBuilder().weakKeys().softValues().build();

    @Override
    public <S extends SchemaNode & TypeAware> T lookupComplex(final S schema) {
        return complexCodecs.getIfPresent(schema);
    }

    @Override
    T lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.getIfPresent(type);
    }

    @Override
    <S extends SchemaNode & TypeAware> T getComplex(final S schema, final T codec) {
        return complexCodecs.get(schema, unused -> codec);
    }

    @Override
    T getSimple(final TypeDefinition<?> type, final T codec) {
        return simpleCodecs.get(type, unused -> codec);
    }
}
