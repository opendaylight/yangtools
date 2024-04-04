/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A type-to-codec factory base class with logic to efficiently lookup and cache codec instances,
 * also dealing with union type composition. This class is thread-safe as long as its underlying {@link CodecCache}
 * is thread-safe.
 *
 * @param <T> Codec type
 */
public abstract class AbstractCodecFactory<T extends TypeAwareCodec<?, ?, ?>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCodecFactory.class);

    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull CodecCache<T> cache;

    protected AbstractCodecFactory(final @NonNull EffectiveModelContext modelContext,
            final @NonNull CodecCache<T> cache) {
        this.modelContext = requireNonNull(modelContext);
        this.cache = requireNonNull(cache);
    }

    public final <S extends TypeAware & SchemaNode> @NonNull T codecFor(final S schema,
            final LeafrefResolver resolver) {
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
         * We assume prevalence is in above order and that caching is effective.
         */
        final var type = schema.getType();
        T ret = cache.lookupSimple(type);
        if (ret != null) {
            LOG.trace("Type {} hit simple {}", type, ret);
            return ret;
        }
        ret = cache.lookupComplex(schema);
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
        ret = createComplexCodecFor(schema, type, resolver);
        LOG.trace("Type {} miss complex {}", type, ret);
        return cache.getComplex(schema, ret);
    }

    /**
     * Return the {@link EffectiveModelContext} backing this factory.
     *
     * @return the {@link EffectiveModelContext} backing this factory
     */
    public final @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }

    protected abstract T binaryCodec(BinaryTypeDefinition type);

    protected abstract T booleanCodec(BooleanTypeDefinition type);

    protected abstract T bitsCodec(BitsTypeDefinition type);

    protected abstract T emptyCodec(EmptyTypeDefinition type);

    protected abstract T enumCodec(EnumTypeDefinition type);

    protected abstract T identityRefCodec(IdentityrefTypeDefinition type, QNameModule module);

    // FIXME: there really are two favors, as 'require-instance true' needs to be validated. In order to deal
    //        with that, though, we need access to the current data store.
    protected abstract T instanceIdentifierCodec(InstanceIdentifierTypeDefinition type);

    /**
     * Return a {@link TypeAwareCodec} capable of serialization and deserialization of {@link YangInstanceIdentifier}s
     * bound to this factory.
     *
     * @return A codec
     */
    public abstract @NonNull TypeAwareCodec<YangInstanceIdentifier, ?, ?> instanceIdentifierCodec();

    protected abstract T int8Codec(Int8TypeDefinition type);

    protected abstract T int16Codec(Int16TypeDefinition type);

    protected abstract T int32Codec(Int32TypeDefinition type);

    protected abstract T int64Codec(Int64TypeDefinition type);

    protected abstract T decimalCodec(DecimalTypeDefinition type);

    protected abstract T stringCodec(StringTypeDefinition type);

    protected abstract T uint8Codec(Uint8TypeDefinition type);

    protected abstract T uint16Codec(Uint16TypeDefinition type);

    protected abstract T uint32Codec(Uint32TypeDefinition type);

    protected abstract T uint64Codec(Uint64TypeDefinition type);

    protected abstract T unionCodec(UnionTypeDefinition type, List<T> codecs);

    private T getSimpleCodecFor(final TypeDefinition<?> type) {
        // These types are expected to be fully-shared
        if (type instanceof EmptyTypeDefinition emptyType) {
            return emptyCodec(emptyType);
        }

        // Now deal with simple types. Note we consider union composed of purely simple types a simple type itself.
        // The checks here are optimized for common types.
        final T ret;
        if (type instanceof StringTypeDefinition stringType) {
            ret = stringCodec(stringType);
        } else if (type instanceof Int8TypeDefinition int8type) {
            ret = int8Codec(int8type);
        } else if (type instanceof Int16TypeDefinition int16type) {
            ret = int16Codec(int16type);
        } else if (type instanceof Int32TypeDefinition int32type) {
            ret = int32Codec(int32type);
        } else if (type instanceof Int64TypeDefinition int64type) {
            ret = int64Codec(int64type);
        } else if (type instanceof Uint8TypeDefinition uint8type) {
            ret = uint8Codec(uint8type);
        } else if (type instanceof Uint16TypeDefinition uint16type) {
            ret = uint16Codec(uint16type);
        } else if (type instanceof Uint32TypeDefinition uint32type) {
            ret = uint32Codec(uint32type);
        } else if (type instanceof Uint64TypeDefinition uint64type) {
            ret = uint64Codec(uint64type);
        } else if (type instanceof BooleanTypeDefinition booleanType) {
            ret = booleanCodec(booleanType);
        } else if (type instanceof DecimalTypeDefinition decimalType) {
            ret = decimalCodec(decimalType);
        } else if (type instanceof EnumTypeDefinition enumType) {
            ret = enumCodec(enumType);
        } else if (type instanceof BitsTypeDefinition bitsType) {
            ret = bitsCodec(bitsType);
        } else if (type instanceof UnionTypeDefinition unionType) {
            if (!isSimpleUnion(unionType)) {
                return null;
            }
            ret = createSimpleUnion(unionType);
        } else if (type instanceof BinaryTypeDefinition binaryType) {
            ret = binaryCodec(binaryType);
        } else if (type instanceof InstanceIdentifierTypeDefinition iidType) {
            return instanceIdentifierCodec(iidType);
        } else {
            return null;
        }

        return cache.getSimple(type, verifyNotNull(ret));
    }

    private static boolean isSimpleUnion(final UnionTypeDefinition union) {
        for (var t : union.getTypes()) {
            if (t instanceof IdentityrefTypeDefinition || t instanceof LeafrefTypeDefinition
                || t instanceof UnionTypeDefinition unionType && !isSimpleUnion(unionType)) {
                LOG.debug("Type {} has non-simple subtype", t);
                return false;
            }
        }

        LOG.debug("Type {} is simple", union);
        return true;
    }

    private T createComplexCodecFor(final SchemaNode schema, final TypeDefinition<?> type,
            final LeafrefResolver resolver) {
        if (type instanceof UnionTypeDefinition union) {
            return createComplexUnion(schema, union, resolver);
        } else if (type instanceof LeafrefTypeDefinition leafref) {
            final var target = resolver.resolveLeafref(leafref);
            final T ret = getSimpleCodecFor(target);
            return ret != null ? ret : createComplexCodecFor(schema, target, resolver);
        } else if (type instanceof IdentityrefTypeDefinition identityref) {
            return identityRefCodec(identityref, schema.getQName().getModule());
        } else {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
    }

    private T createSimpleUnion(final UnionTypeDefinition union) {
        final var types = union.getTypes();
        final var codecs = new ArrayList<T>(types.size());

        for (var type : types) {
            T codec = cache.lookupSimple(type);
            if (codec == null) {
                codec = verifyNotNull(getSimpleCodecFor(type), "Type %s did not resolve to a simple codec", type);
            }

            codecs.add(codec);
        }

        return unionCodec(union, codecs);
    }

    private T createComplexUnion(final SchemaNode schema, final UnionTypeDefinition union,
            final LeafrefResolver resolver) {
        final var types = union.getTypes();
        final var codecs = new ArrayList<T>(types.size());

        for (var type : types) {
            T codec = cache.lookupSimple(type);
            if (codec == null) {
                codec = getSimpleCodecFor(type);
                if (codec == null) {
                    codec = createComplexCodecFor(schema, type, resolver);
                }
            }

            codecs.add(verifyNotNull(codec, "Type %s has no codec", schema, type));
        }

        return unionCodec(union, codecs);
    }
}
