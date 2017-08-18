/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.LazyCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.NoopCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.PrecomputedCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.SharedCodecCache;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link SchemaContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 *
 * There are multiple implementations available, each with distinct thread-safety, CPU/memory trade-offs and reuse
 * characteristics. See {@link #getShared(SchemaContext)}, {@link #getPrecomputed(SchemaContext)},
 * {@link #createLazy(SchemaContext)} and {@link #createSimple(SchemaContext)} for details.
 */
@Beta
public final class JSONCodecFactory extends AbstractCodecFactory<JSONCodec<?>> {
    private static final class EagerCacheLoader extends CacheLoader<SchemaContext, JSONCodecFactory> {
        @Override
        public JSONCodecFactory load(final SchemaContext key) {
            final Stopwatch sw = Stopwatch.createStarted();
            final LazyCodecCache<JSONCodec<?>> lazyCache = new LazyCodecCache<>();
            final JSONCodecFactory lazy = new JSONCodecFactory(key, lazyCache);
            final int visitedLeaves = requestCodecsForChildren(lazy, key);
            sw.stop();

            final PrecomputedCodecCache<JSONCodec<?>> cache = lazyCache.toPrecomputed();
            LOG.debug("{} leaf nodes resulted in {} simple and {} complex codecs in {}", visitedLeaves,
                cache.simpleSize(), cache.complexSize(), sw);
            return new JSONCodecFactory(key, cache);
        }

        private static int requestCodecsForChildren(final JSONCodecFactory lazy, final DataNodeContainer parent) {
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

    private static final Logger LOG = LoggerFactory.getLogger(JSONCodecFactory.class);

    // Weak keys to retire the entry when SchemaContext goes away
    private static final LoadingCache<SchemaContext, JSONCodecFactory> PRECOMPUTED = CacheBuilder.newBuilder()
            .weakKeys().build(new EagerCacheLoader());

    // Weak keys to retire the entry when SchemaContext goes away and to force identity-based lookup
    private static final LoadingCache<SchemaContext, JSONCodecFactory> SHARED = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<SchemaContext, JSONCodecFactory>() {
                @Override
                public JSONCodecFactory load(final SchemaContext key) {
                    return new JSONCodecFactory(key, new SharedCodecCache<>());
                }
            });

    private final JSONCodec<?> iidCodec;

    JSONCodecFactory(final SchemaContext context, final CodecCache<JSONCodec<?>> cache) {
        super(context, cache);
        iidCodec = new JSONStringInstanceIdentifierCodec(context, this);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     *
     * @deprecated Use {@link #getShared(SchemaContext)} instead.
     */
    @Deprecated
    public static JSONCodecFactory create(final SchemaContext context) {
        return getShared(context);
    }

    /**
     * Get a thread-safe, eagerly-caching {@link JSONCodecFactory} for a SchemaContext. This method can, and will,
     * return the same instance as long as the associated SchemaContext is present. Returned object can be safely
     * used by multiple threads concurrently. If the SchemaContext instance does not have a cached instance
     * of {@link JSONCodecFactory}, it will be completely precomputed before this method will return.
     *
     * Choosing this implementation is appropriate when the memory overhead of keeping a full codec tree is not as
     * great a concern as predictable performance. When compared to the implementation returned by
     * {@link #getShared(SchemaContext)}, this implementation is expected to offer higher performance and have lower
     * peak memory footprint when most of the SchemaContext is actually in use.
     *
     * For call sites which do not want to pay the CPU cost of pre-computing this implementation, but still would like
     * to use it if is available (by being populated by some other caller), you can use
     * {@link #getPrecomputedIfAvailable(SchemaContext)}.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public static JSONCodecFactory getPrecomputed(final SchemaContext context) {
        return PRECOMPUTED.getUnchecked(context);
    }

    /**
     * Get a thread-safe, eagerly-caching {@link JSONCodecFactory} for a SchemaContext, if it is available. This
     * method is a non-blocking equivalent of {@link #getPrecomputed(SchemaContext)} for use in code paths where
     * the potential of having to pre-compute the implementation is not acceptable. One such scenario is when the
     * code base wants to opportunistically take advantage of pre-computed version, but is okay with a fallback to
     * a different implementation.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}, or absent if such an implementation is not available.
     * @throws NullPointerException if context is null
     */
    public static Optional<JSONCodecFactory> getPrecomputedIfAvailable(final SchemaContext context) {
        return Optional.ofNullable(PRECOMPUTED.getIfPresent(context));
    }

    /**
     * Get a thread-safe, lazily-caching {@link JSONCodecFactory} for a SchemaContext. This method can, and will,
     * return the same instance as long as the associated SchemaContext is present or the factory is not invalidated
     * by memory pressure. Returned object can be safely used by multiple threads concurrently.
     *
     * Choosing this implementation is a safe default, as it will not incur prohibitive blocking, nor will it tie up
     * memory in face of pressure.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public static JSONCodecFactory getShared(final SchemaContext context) {
        return SHARED.getUnchecked(context);
    }

    /**
     * Create a new thread-unsafe, lazily-caching {@link JSONCodecFactory} for a SchemaContext. This method will
     * return distinct objects every time it is invoked. Returned object may not be used from multiple threads
     * concurrently.
     *
     * This implementation is appropriate for one-off serialization from a single thread. It will aggressively cache
     * codecs for reuse and will tie them up in memory until the factory is freed.
     *
     * @param context SchemaContext instance
     * @return A non-sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public static JSONCodecFactory createLazy(final SchemaContext context) {
        return new JSONCodecFactory(context, new LazyCodecCache<>());
    }

    /**
     * Create a simplistic, thread-safe {@link JSONCodecFactory} for a {@link SchemaContext}. This method will return
     * distinct objects every time it is invoked. Returned object may be use from multiple threads concurrently.
     *
     * This implementation exists mostly for completeness only, as it does not perform any caching at all and each codec
     * is computed every time it is requested. This may be useful in extremely constrained environments, where memory
     * footprint is more critical than performance.
     *
     * @param context SchemaContext instance
     * @return A non-sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null.
     */
    public static JSONCodecFactory createSimple(final SchemaContext context) {
        return new JSONCodecFactory(context, NoopCodecCache.getInstance());
    }

    @Override
    protected JSONCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedJSONCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanJSONCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedJSONCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new NumberJSONCodec<>(DecimalStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyJSONCodec.INSTANCE;
    }

    @Override
    protected JSONCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedJSONCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefJSONCodec(getSchemaContext(), module);
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        // FIXME: there really are two favors, as 'require-instance true' needs to be validated. In order to deal
        //        with that, though, we need access to the current data store.
        return iidCodec;
    }

    @Override
    protected JSONCodec<?> intCodec(final IntegerTypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uintCodec(final UnsignedIntegerTypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> unionCodec(final UnionTypeDefinition type, final List<JSONCodec<?>> codecs) {
        return UnionJSONCodec.create(type, codecs);
    }

    @Override
    protected JSONCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullJSONCodec.INSTANCE;
    }
}
