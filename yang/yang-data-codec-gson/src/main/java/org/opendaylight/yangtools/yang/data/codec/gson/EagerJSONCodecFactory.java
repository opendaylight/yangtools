/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
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

/**
 * Pre-computed JSONCodecFactory. All possible codecs are created upfront at instantiation time, after which they
 * are available for the cost of a constant lookup.
 *
 * @author Robert Varga
 */
@ThreadSafe
final class EagerJSONCodecFactory extends JSONCodecFactory {
    // Weak keys to retire the entry when SchemaContext goes away
    // Soft values to keep unreferenced factories around for a bit
    private static final LoadingCache<SchemaContext, EagerJSONCodecFactory> CACHE = CacheBuilder.newBuilder()
            .weakKeys().softValues().build(new CacheLoader<SchemaContext, EagerJSONCodecFactory>() {
                @Override
                public EagerJSONCodecFactory load(final SchemaContext key) {
                    return new EagerJSONCodecFactory(key);
                }
            });

    private final Map<TypedSchemaNode, JSONCodec<?>> codecs;

    EagerJSONCodecFactory(final SchemaContext context) {
        super(context);
        this.codecs = constructCodecs(context);
    }

    static EagerJSONCodecFactory getIfPresent(final SchemaContext context) {
        return CACHE.getIfPresent(context);
    }

    static EagerJSONCodecFactory get(final SchemaContext context) {
        return CACHE.getUnchecked(context);
    }

    private static Map<TypedSchemaNode, JSONCodec<?>> constructCodecs(final SchemaContext context) {
        final LazyJSONCodecFactory lazy = new LazyJSONCodecFactory(context);
        requestCodecsForChildren(lazy, context);
        return lazy.getCodecs();
    }

    private static void requestCodecsForChildren(final LazyJSONCodecFactory factory, final DataNodeContainer parent) {
        for (DataSchemaNode child : parent.getChildNodes()) {
            if (child instanceof TypedSchemaNode) {
                factory.codecFor(child);
            } else if (child instanceof DataNodeContainer) {
                requestCodecsForChildren(factory, (DataNodeContainer)child);
            }
        }
    }

    @Override
    JSONCodec<?> lookupComplex(final TypedSchemaNode schema) {
        final JSONCodec<?> ret = codecs.get(schema);
        Preconditions.checkArgument(ret != null, "No codec available for schema %s", schema);
        return ret;
    }

    @Override
    JSONCodec<?> lookupSimple(final TypeDefinition<?> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    JSONCodec<?> getComplex(final TypedSchemaNode schema, final JSONCodec<?> codec) {
        // This should never be called
        throw new UnsupportedOperationException();
    }

    @Override
    JSONCodec<?> getSimple(final TypeDefinition<?> type, final JSONCodec<?> codec) {
        // This should never be called
        throw new UnsupportedOperationException();
    }
}
