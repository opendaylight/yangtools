/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link SchemaContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 *
 * <p>
 * There are multiple implementations available, each with distinct thread-safety, CPU/memory trade-offs and reuse
 * characteristics. See {@link #getShared(SchemaContext)}, {@link #getPrecomputed(SchemaContext)},
 * {@link #createLazy(SchemaContext)} and {@link #createSimple(SchemaContext)} for details.
 */
@Beta
public abstract class JSONCodecFactory extends AbstractCodecFactory<JSONCodec<?>> {
    JSONCodecFactory(final @NonNull SchemaContext context, final @NonNull CodecCache<JSONCodec<?>> cache) {
        super(context, cache);
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
     * {@link #getShared(SchemaContext)}, this implementation is expected to offer higher performance and have lower
     * peak memory footprint when most of the SchemaContext is actually in use.
     *
     * <p>
     * For call sites which do not want to pay the CPU cost of pre-computing this implementation, but still would like
     * to use it if is available (by being populated by some other caller), you can use
     * {@link #getPrecomputedIfAvailable(SchemaContext)}.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     *
     * @deprecated Use {@link JSONCodecFactorySupplier#getPrecomputed(SchemaContext)} instead.
     */
    @Deprecated
    public static JSONCodecFactory getPrecomputed(final SchemaContext context) {
        return JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getPrecomputed(context);
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
     *
     * @deprecated Use {@link JSONCodecFactorySupplier#getPrecomputedIfAvailable(SchemaContext)} instead.
     */
    @Deprecated
    public static Optional<JSONCodecFactory> getPrecomputedIfAvailable(final SchemaContext context) {
        return JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getPrecomputedIfAvailable(context);
    }

    /**
     * Get a thread-safe, lazily-caching {@link JSONCodecFactory} for a SchemaContext. This method can, and will,
     * return the same instance as long as the associated SchemaContext is present or the factory is not invalidated
     * by memory pressure. Returned object can be safely used by multiple threads concurrently.
     *
     * <p>
     * Choosing this implementation is a safe default, as it will not incur prohibitive blocking, nor will it tie up
     * memory in face of pressure.
     *
     * @param context SchemaContext instance
     * @return A sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null
     *
     * @deprecated Use {@link JSONCodecFactorySupplier#getShared(SchemaContext)} instead.
     */
    @Deprecated
    public static JSONCodecFactory getShared(final SchemaContext context) {
        return JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context);
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
     *
     * @deprecated Use {@link JSONCodecFactorySupplier#createLazy(SchemaContext)} instead.
     */
    @Deprecated
    public static JSONCodecFactory createLazy(final SchemaContext context) {
        return JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.createLazy(context);
    }

    /**
     * Create a simplistic, thread-safe {@link JSONCodecFactory} for a {@link SchemaContext}. This method will return
     * distinct objects every time it is invoked. Returned object may be use from multiple threads concurrently.
     *
     * <p>
     * This implementation exists mostly for completeness only, as it does not perform any caching at all and each codec
     * is computed every time it is requested. This may be useful in extremely constrained environments, where memory
     * footprint is more critical than performance.
     *
     * @param context SchemaContext instance
     * @return A non-sharable {@link JSONCodecFactory}
     * @throws NullPointerException if context is null.
     *
     * @deprecated Use {@link JSONCodecFactorySupplier#createSimple(SchemaContext)} instead.
     */
    @Deprecated
    public static JSONCodecFactory createSimple(final SchemaContext context) {
        return JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.createSimple(context);
    }

    @Override
    protected final JSONCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedJSONCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanJSONCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedJSONCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return wrapDecimalCodec(DecimalStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyJSONCodec.INSTANCE;
    }

    @Override
    protected final JSONCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedJSONCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefJSONCodec(getSchemaContext(), module);
    }

    @Override
    protected final JSONCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int64Codec(final Int64TypeDefinition type) {
        return wrapIntegerCodec(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return wrapIntegerCodec(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> unionCodec(final UnionTypeDefinition type, final List<JSONCodec<?>> codecs) {
        return UnionJSONCodec.create(type, codecs);
    }

    @Override
    protected final JSONCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullJSONCodec.INSTANCE;
    }

    abstract JSONCodec<?> wrapDecimalCodec(DecimalStringCodec decimalCodec);

    abstract JSONCodec<?> wrapIntegerCodec(AbstractIntegerStringCodec<?, ?> integerCodec);
}
