/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pre-computed JSONCodecFactory. All possible codecs are created upfront at instantiation time, after which they
 * are available for the cost of a constant lookup.
 *
 * @author Robert Varga
 */
@ThreadSafe
final class EagerJSONCodecFactory extends JSONCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(EagerJSONCodecFactory.class);

    // Weak keys to retire the entry when SchemaContext goes away
    private static final LoadingCache<SchemaContext, EagerJSONCodecFactory> CACHE = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<SchemaContext, EagerJSONCodecFactory>() {
                @Override
                public EagerJSONCodecFactory load(final SchemaContext key) {
                    final Stopwatch sw = Stopwatch.createStarted();
                    final LazyJSONCodecFactory lazy = new LazyJSONCodecFactory(key);
                    final int visitedLeaves = requestCodecsForChildren(lazy, key);
                    sw.stop();

                    final Map<TypeDefinition<?>, JSONCodec<?>> simple = lazy.getSimpleCodecs();
                    final Map<TypedSchemaNode, JSONCodec<?>> complex = lazy.getComplexCodecs();

                    LOG.debug("{} leaf nodes resulted in {} simple and {} complex codecs in {}", visitedLeaves,
                        simple.size(), complex.size(), sw);
                    return new EagerJSONCodecFactory(key, simple, complex);
                }
            });

    private final Map<TypeDefinition<?>, JSONCodec<?>> simpleCodecs;
    private final Map<TypedSchemaNode, JSONCodec<?>> complexCodecs;

    private EagerJSONCodecFactory(final SchemaContext context, final Map<TypeDefinition<?>, JSONCodec<?>> simpleCodecs,
        final Map<TypedSchemaNode, JSONCodec<?>> complexCodecs) {
        super(context);
        this.simpleCodecs = Preconditions.checkNotNull(simpleCodecs);
        this.complexCodecs = Preconditions.checkNotNull(complexCodecs);
    }

    static EagerJSONCodecFactory get(final SchemaContext context) {
        return CACHE.getUnchecked(context);
    }

    static EagerJSONCodecFactory getIfPresent(final SchemaContext context) {
        return CACHE.getIfPresent(context);
    }

    @Override
    JSONCodec<?> lookupComplex(final TypedSchemaNode schema) {
        final JSONCodec<?> ret = complexCodecs.get(schema);
        Preconditions.checkArgument(ret != null, "No codec available for schema %s", schema);
        return ret;
    }

    @Override
    JSONCodec<?> lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.get(type);
    }

    @Override
    JSONCodec<?> getComplex(final TypedSchemaNode schema, final JSONCodec<?> codec) {
        throw new IllegalStateException("Uncached codec for " + schema);
    }

    @Override
    JSONCodec<?> getSimple(final TypeDefinition<?> type, final JSONCodec<?> codec) {
        throw new IllegalStateException("Uncached codec for " + type);
    }

    static int requestCodecsForChildren(final LazyJSONCodecFactory lazy, final DataNodeContainer parent) {
        int ret = 0;
        for (DataSchemaNode child : parent.getChildNodes()) {
            if (child instanceof TypedSchemaNode) {
                lazy.codecFor((TypedSchemaNode) child);
                ++ret;
            } else if (child instanceof DataNodeContainer) {
                ret += requestCodecsForChildren(lazy, (DataNodeContainer) child);
            }
        }

        return ret;
    }
}
