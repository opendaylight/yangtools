/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
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
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
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
public abstract class JSONCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JSONCodecFactory.class);

    private final SchemaContext schemaContext;
    private final JSONCodec<?> iidCodec;

    JSONCodecFactory(final SchemaContext context) {
        this.schemaContext = Preconditions.checkNotNull(context);
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
        return EagerJSONCodecFactory.get(context);
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
        return Optional.fromNullable(EagerJSONCodecFactory.getIfPresent(context));
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
        return SharedJSONCodecFactory.get(context);
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
        return new LazyJSONCodecFactory(context);
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
        return new SimpleJSONCodecFactory(context);
    }

    final JSONCodec<?> codecFor(final TypedSchemaNode schema) {
        /*
         * There are many trade-offs to be made here. We need the common case being as fast as possible while reusing
         * codecs as much as possible.
         *
         * This gives us essentially four classes of codecs:
         * - simple codecs, which are based on the type definition only
         * - complex codecs, which depend on both type definition and the leaf
         * - null codec, which does not depend on anything
         * - instance identifier codec, which is based on namespace mapping
         *
         * We assume prevalence is in above order and that caching is effective. We therefore
         */
        final TypeDefinition<?> type = schema.getType();
        JSONCodec<?> ret = lookupSimple(type);
        if (ret != null) {
            LOG.trace("Type {} hit simple {}", type, ret);
            return ret;
        }
        ret = lookupComplex(schema);
        if (ret != null) {
            LOG.trace("Type {} hit complex {}", type, ret);
            return ret;
        }

        // Dealing with simple types first...
        ret = getSimpleCodecFor(type);
        if (ret != null) {
            LOG.trace("Type {} miss simple {}", type, ret);
            return ret;
        }

        // ... and complex types afterwards
        ret = createComplexCodecFor(schema, type);
        LOG.trace("Type {} miss complex {}", type, ret);
        return getComplex(schema, ret);
    }

    final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    /**
     * Lookup a complex codec for schema node.
     *
     * @param schema Schema node
     * @return Cached codec, or null if no codec is cached.
     */
    @Nullable abstract JSONCodec<?> lookupComplex(TypedSchemaNode schema);

    /**
     * Lookup a simple codec for a type definition.
     *
     * @param type Type definitions
     * @return Cached codec, or null if no codec is cached.
     */
    @Nullable abstract JSONCodec<?> lookupSimple(TypeDefinition<?> type);

    /**
     * Lookup-or-store a complex codec for a particular schema node.
     *
     * @param schema Schema node
     * @param codec Codec to cache
     * @return Codec instance, either already-cached, or the codec presented as argument.
     */
    @Nonnull abstract JSONCodec<?> getComplex(TypedSchemaNode schema, JSONCodec<?> codec);

    /**
     * Lookup-or-store a simple codec for a particular schema node.
     *
     * @param schema Schema node
     * @param codec Codec to cache
     * @return Codec instance, either already-cached, or the codec presented as argument.
     */
    @Nonnull abstract JSONCodec<?> getSimple(TypeDefinition<?> type, JSONCodec<?> codec);

    private JSONCodec<?> getSimpleCodecFor(final TypeDefinition<?> type) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
            // FIXME: there really are two favors, as 'require-instance true' needs to be validated. In order to deal
            //        with that, though, we need access to the current data store.
            return iidCodec;
        } else if (type instanceof EmptyTypeDefinition) {
            return EmptyJSONCodec.INSTANCE;
        } else if (type instanceof UnknownTypeDefinition) {
            return NullJSONCodec.INSTANCE;
        }

        // Now deal with simple types. Note we consider union composed of purely simple types a simple type itself.
        // The checks here are optimized for common types.
        final JSONCodec<?> ret;
        if (type instanceof StringTypeDefinition) {
            ret = new QuotedJSONCodec<>(StringStringCodec.from((StringTypeDefinition) type));
        } else if (type instanceof IntegerTypeDefinition) {
            ret = new NumberJSONCodec<>(AbstractIntegerStringCodec.from((IntegerTypeDefinition) type));
        } else if (type instanceof UnsignedIntegerTypeDefinition) {
            ret = new NumberJSONCodec<>(AbstractIntegerStringCodec.from((UnsignedIntegerTypeDefinition) type));
        } else if (type instanceof BooleanTypeDefinition) {
            ret = new BooleanJSONCodec(BooleanStringCodec.from((BooleanTypeDefinition) type));
        } else if (type instanceof DecimalTypeDefinition) {
            ret = new NumberJSONCodec<>(DecimalStringCodec.from((DecimalTypeDefinition) type));
        } else if (type instanceof EnumTypeDefinition) {
            ret = new QuotedJSONCodec<>(EnumStringCodec.from((EnumTypeDefinition) type));
        } else if (type instanceof BitsTypeDefinition) {
            ret = new QuotedJSONCodec<>(BitsStringCodec.from((BitsTypeDefinition) type));
        } else if (type instanceof UnionTypeDefinition) {
            final UnionTypeDefinition union = (UnionTypeDefinition) type;
            if (!isSimpleUnion(union)) {
                return null;
            }
            ret = createSimpleUnion(union);
        } else if (type instanceof BinaryTypeDefinition) {
            ret = new QuotedJSONCodec<>(BinaryStringCodec.from((BinaryTypeDefinition) type));
        } else {
            return null;
        }

        return getSimple(type, Verify.verifyNotNull(ret));
    }

    private JSONCodec<?> createComplexCodecFor(final TypedSchemaNode schema, final TypeDefinition<?> type) {
        if (type instanceof UnionTypeDefinition) {
            return createComplexUnion(schema, (UnionTypeDefinition) type);
        } else if (type instanceof LeafrefTypeDefinition) {
            final TypeDefinition<?> target = SchemaContextUtil.getBaseTypeForLeafRef((LeafrefTypeDefinition) type,
                schemaContext, schema);
            Verify.verifyNotNull(target, "Unable to find base type for leafref node %s type %s.", schema.getPath(),
                    target);

            final JSONCodec<?> ret = getSimpleCodecFor(target);
            return ret != null ? ret : createComplexCodecFor(schema, target);
        } else if (type instanceof IdentityrefTypeDefinition) {
            return new JSONStringIdentityrefCodec(schemaContext, schema.getQName().getModule());
        } else {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
    }

    private static boolean isSimpleUnion(final UnionTypeDefinition union) {
        for (TypeDefinition<?> t : union.getTypes()) {
            if (t instanceof IdentityrefTypeDefinition || t instanceof LeafrefTypeDefinition
                    || (t instanceof UnionTypeDefinition && !isSimpleUnion((UnionTypeDefinition) t))) {
                LOG.debug("Type {} has non-simple subtype", t);
                return false;
            }
        }

        LOG.debug("Type {} is simple", union);
        return true;
    }

    private JSONCodec<?> createSimpleUnion(final UnionTypeDefinition union) {
        final List<TypeDefinition<?>> types = union.getTypes();
        final List<JSONCodec<?>> codecs = new ArrayList<>(types.size());

        for (TypeDefinition<?> type : types) {
            JSONCodec<?> codec = lookupSimple(type);
            if (codec == null) {
                codec = Verify.verifyNotNull(getSimpleCodecFor(type), "Type %s did not resolve to a simple codec",
                    type);
            }

            codecs.add(codec);
        }

        return UnionJSONCodec.create(union, codecs);
    }

    private UnionJSONCodec<?> createComplexUnion(final TypedSchemaNode schema, final UnionTypeDefinition union) {
        final List<TypeDefinition<?>> types = union.getTypes();
        final List<JSONCodec<?>> codecs = new ArrayList<>(types.size());

        for (TypeDefinition<?> type : types) {
            JSONCodec<?> codec = lookupSimple(type);
            if (codec == null) {
                codec = getSimpleCodecFor(type);
                if (codec == null) {
                    codec = createComplexCodecFor(schema, type);
                }
            }

            codecs.add(Verify.verifyNotNull(codec, "Schema %s subtype %s has no codec", schema, type));
        }

        return UnionJSONCodec.create(union, codecs);
    }
}
