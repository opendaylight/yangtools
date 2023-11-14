/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.LazyCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.NoopCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.PrecomputedCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.SharedCodecCache;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API entry point for acquiring {@link JSONCodecFactory} instances.
 *
 * @author Robert Varga
 */
public enum JSONCodecFactorySupplier {
    /**
     * Source of {@link JSONCodecFactory} instances compliant with
     * <a href="https://www.rfc-editor.org/rfc/rfc7951">RFC7951</a>.
     */
    RFC7951() {
        @Override
        JSONCodecFactory createFactory(final EffectiveModelContext context, final CodecCache<JSONCodec<?>> cache) {
            return new JSONCodecFactory.RFC7951(context, cache);
        }
    },
    /**
     * Source of {@link JSONCodecFactory} instances compliant with {@code draft-lhotka-netmod-yang-json-02}.
     *
     * @deprecated This is a historic implementation, retained for compatibility. Please migrate to {@link #RFC7951}.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    DRAFT_LHOTKA_NETMOD_YANG_JSON_02() {
        @Override
        JSONCodecFactory createFactory(final EffectiveModelContext context, final CodecCache<JSONCodec<?>> cache) {
            return new JSONCodecFactory.Lhotka02(context, cache);
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(JSONCodecFactorySupplier.class);

    private static final class EagerCacheLoader extends CacheLoader<EffectiveModelContext, JSONCodecFactory> {
        private final BiFunction<EffectiveModelContext, CodecCache<JSONCodec<?>>, JSONCodecFactory> factorySupplier;

        EagerCacheLoader(final BiFunction<EffectiveModelContext,
                CodecCache<JSONCodec<?>>, JSONCodecFactory> factorySupplier) {
            this.factorySupplier = requireNonNull(factorySupplier);
        }

        @Override
        public JSONCodecFactory load(final EffectiveModelContext key) {
            final Stopwatch sw = Stopwatch.createStarted();
            final LazyCodecCache<JSONCodec<?>> lazyCache = new LazyCodecCache<>();
            final JSONCodecFactory lazy = factorySupplier.apply(key, lazyCache);
            final SchemaInferenceStack stack = SchemaInferenceStack.of(key);

            int visitedLeaves = 0;
            for (ModuleEffectiveStatement module : key.getModuleStatements().values()) {
                visitedLeaves += codecsForChildren(lazy, stack, module);
                stack.clear();
            }
            sw.stop();

            final PrecomputedCodecCache<JSONCodec<?>> cache = lazyCache.toPrecomputed();
            LOG.debug("{} leaf nodes resulted in {} simple and {} complex codecs in {}", visitedLeaves,
                cache.simpleSize(), cache.complexSize(), sw);
            return factorySupplier.apply(key, cache);
        }

        private static int codecsForChildren(final JSONCodecFactory lazy, final SchemaInferenceStack stack,
                final DataTreeAwareEffectiveStatement<?, ?> parent) {
            int ret = 0;
            for (var child : parent.dataTreeNodes()) {
                if (child instanceof DataTreeAwareEffectiveStatement<?, ?> dataTree) {
                    stack.enterDataTree(child.argument());
                    ret += codecsForChildren(lazy, stack, dataTree);
                    stack.exit();
                } else if (child instanceof TypedDataSchemaNode typed) {
                    lazy.codecFor(typed, stack);
                    ++ret;
                }
            }

            return ret;
        }
    }

    // Weak keys to retire the entry when SchemaContext goes away
    private final LoadingCache<EffectiveModelContext, JSONCodecFactory> precomputed;

    // Weak keys to retire the entry when SchemaContext goes away and to force identity-based lookup
    private final LoadingCache<EffectiveModelContext, JSONCodecFactory> shared = CacheBuilder.newBuilder()
        .weakKeys().build(new CacheLoader<EffectiveModelContext, JSONCodecFactory>() {
            @Override
            public JSONCodecFactory load(final EffectiveModelContext key) {
                return createFactory(key, new SharedCodecCache<>());
            }
        });

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
        justification = "https://github.com/spotbugs/spotbugs/issues/1867")
    JSONCodecFactorySupplier() {
        precomputed = CacheBuilder.newBuilder().weakKeys().build(new EagerCacheLoader(this::createFactory));
    }

    /**
     * Get a thread-safe, eagerly-caching {@link JSONCodecFactory} for a SchemaContext. This method can, and will,
     * return the same instance as long as the associated SchemaContext is present. Returned object can be safely
     * used by multiple threads concurrently. If the SchemaContext instance does not have a cached instance
     * of {@link JSONCodecFactory}, it will be completely precomputed before this method will return.
     *
     * <p>
     * Choosing this implementation is appropriate when the memory overhead of keeping a full codec tree is not as
     * great a concern as predictable performance. When compared to the implementation returned by
     * {@link #getShared(EffectiveModelContext)}, this implementation is expected to offer higher performance and have
     * lower peak memory footprint when most of the SchemaContext is actually in use.
     *
     * <p>
     * For call sites which do not want to pay the CPU cost of pre-computing this implementation, but still would like
     * to use it if is available (by being populated by some other caller), you can use
     * {@link #getPrecomputedIfAvailable(EffectiveModelContext)}.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public @NonNull JSONCodecFactory getPrecomputed(final @NonNull EffectiveModelContext context) {
        return verifyNotNull(precomputed.getUnchecked(context));
    }

    /**
     * Get a thread-safe, eagerly-caching {@link JSONCodecFactory} for a SchemaContext, if it is available. This
     * method is a non-blocking equivalent of {@link #getPrecomputed(EffectiveModelContext)} for use in code paths where
     * the potential of having to pre-compute the implementation is not acceptable. One such scenario is when the
     * code base wants to opportunistically take advantage of pre-computed version, but is okay with a fallback to
     * a different implementation.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}, or absent if such an implementation is not available.
     * @throws NullPointerException if context is null
     */
    public @NonNull Optional<JSONCodecFactory> getPrecomputedIfAvailable(final @NonNull EffectiveModelContext context) {
        return Optional.ofNullable(precomputed.getIfPresent(context));
    }

    /**
     * Get a thread-safe, lazily-caching {@link JSONCodecFactory} for a SchemaContext. This method can, and will,
     * return the same instance as long as the associated EffectiveModelContext is present or the factory is not
     * invalidated by memory pressure. Returned object can be safely used by multiple threads concurrently.
     *
     * <p>
     * Choosing this implementation is a safe default, as it will not incur prohibitive blocking, nor will it tie up
     * memory in face of pressure.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public @NonNull JSONCodecFactory getShared(final @NonNull EffectiveModelContext context) {
        return verifyNotNull(shared.getUnchecked(context));
    }

    /**
     * Create a new thread-unsafe, lazily-caching {@link JSONCodecFactory} for a SchemaContext. This method will
     * return distinct objects every time it is invoked. Returned object may not be used from multiple threads
     * concurrently.
     *
     * <p>
     * This implementation is appropriate for one-off serialization from a single thread. It will aggressively cache
     * codecs for reuse and will tie them up in memory until the factory is freed.
     *
     * @param context SchemaContext instance
     * @return A non-sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     */
    public @NonNull JSONCodecFactory createLazy(final @NonNull EffectiveModelContext context) {
        return createFactory(context, new LazyCodecCache<>());
    }

    /**
     * Create a simplistic, thread-safe {@link JSONCodecFactory} for a {@link EffectiveModelContext}. This method will
     * return distinct objects every time it is invoked. Returned object may be use from multiple threads concurrently.
     *
     * <p>
     * This implementation exists mostly for completeness only, as it does not perform any caching at all and each codec
     * is computed every time it is requested. This may be useful in extremely constrained environments, where memory
     * footprint is more critical than performance.
     *
     * @param context SchemaContext instance
     * @return A non-sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null.
     */
    public @NonNull JSONCodecFactory createSimple(final @NonNull EffectiveModelContext context) {
        return createFactory(context, NoopCodecCache.getInstance());
    }

    abstract @NonNull JSONCodecFactory createFactory(EffectiveModelContext context, CodecCache<JSONCodec<?>> cache);
}
