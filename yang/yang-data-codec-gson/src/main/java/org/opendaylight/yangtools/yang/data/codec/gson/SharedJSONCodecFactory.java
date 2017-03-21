/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

/**
 * A thread-safe lazily-populated codec factory. Instances are cached in an internal weak/soft cache.
 *
 * @author Robert Varga
 */
@ThreadSafe
final class SharedJSONCodecFactory extends JSONCodecFactory {
    // Weak keys to retire the entry when SchemaContext goes away and to force identity-based lookup
    private static final LoadingCache<SchemaContext, SharedJSONCodecFactory> CACHE = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<SchemaContext, SharedJSONCodecFactory>() {
                @Override
                public SharedJSONCodecFactory load(final SchemaContext key) {
                    return new SharedJSONCodecFactory(key);
                }
            });

    // Weak keys to force identity lookup
    // Soft values to keep unreferenced codecs around for a bit, but eventually we want them to go away
    private final Cache<TypeDefinition<?>, JSONCodec<?>> simpleCodecs = CacheBuilder.newBuilder().weakKeys()
            .softValues().build();
    private final Cache<TypedSchemaNode, JSONCodec<?>> complexCodecs = CacheBuilder.newBuilder().weakKeys().softValues()
            .build();

    SharedJSONCodecFactory(final SchemaContext context) {
        super(context);
    }

    static SharedJSONCodecFactory getIfPresent(final SchemaContext context) {
        return CACHE.getIfPresent(context);
    }

    static SharedJSONCodecFactory get(final SchemaContext context) {
        return CACHE.getUnchecked(context);
    }

    @Override
    JSONCodec<?> lookupComplex(final TypedSchemaNode schema) {
        return complexCodecs.getIfPresent(schema);
    }

    @Override
    JSONCodec<?> lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.getIfPresent(type);
    }

    @Override
    JSONCodec<?> getComplex(final TypedSchemaNode schema, final JSONCodec<?> codec) {
        try {
            return complexCodecs.get(schema, () -> codec);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    @Override
    JSONCodec<?> getSimple(final TypeDefinition<?> type, final JSONCodec<?> codec) {
        try {
            return simpleCodecs.get(type, () -> codec);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }
}
