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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
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
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A type-to-codec factory base class with logic to efficiently lookup and cache codec instances,
 * also dealing with union type composition. This class is thread-safe as long as its underlying {@link CodecCache}
 * is thread-safe
 *
 * @param <T> Codec type
 * @author Robert Varga
 */
public abstract class AbstractCodecFactory<T extends TypeAwareCodec<?, ?, ?>> implements SchemaContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCodecFactory.class);

    private final @NonNull CodecCache<T> cache;

    private final @NonNull SchemaContext schemaContext;

    protected AbstractCodecFactory(final SchemaContext schemaContext, final CodecCache<T> cache) {
        this.schemaContext = requireNonNull(schemaContext);
        this.cache = requireNonNull(cache);
    }

    @Override
    public final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public final <S extends TypeAware & SchemaNode> @NonNull T codecFor(final S schema) {
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
        final TypeDefinition<?> type = schema.getType();
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
        ret = createComplexCodecFor(schema, type);
        LOG.trace("Type {} miss complex {}", type, ret);
        return cache.getComplex(schema, ret);
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

    protected abstract T unknownCodec(UnknownTypeDefinition type);

    private T getSimpleCodecFor(final TypeDefinition<?> type) {
        // These types are expected to be fully-shared
        if (type instanceof EmptyTypeDefinition) {
            return emptyCodec((EmptyTypeDefinition) type);
        } else if (type instanceof UnknownTypeDefinition) {
            return unknownCodec((UnknownTypeDefinition) type);
        }

        // Now deal with simple types. Note we consider union composed of purely simple types a simple type itself.
        // The checks here are optimized for common types.
        final T ret;
        if (type instanceof StringTypeDefinition) {
            ret = stringCodec((StringTypeDefinition) type);
        } else if (type instanceof Int8TypeDefinition) {
            ret = int8Codec((Int8TypeDefinition) type);
        } else if (type instanceof Int16TypeDefinition) {
            ret = int16Codec((Int16TypeDefinition) type);
        } else if (type instanceof Int32TypeDefinition) {
            ret = int32Codec((Int32TypeDefinition) type);
        } else if (type instanceof Int64TypeDefinition) {
            ret = int64Codec((Int64TypeDefinition) type);
        } else if (type instanceof Uint8TypeDefinition) {
            ret = uint8Codec((Uint8TypeDefinition) type);
        } else if (type instanceof Uint16TypeDefinition) {
            ret = uint16Codec((Uint16TypeDefinition) type);
        } else if (type instanceof Uint32TypeDefinition) {
            ret = uint32Codec((Uint32TypeDefinition) type);
        } else if (type instanceof Uint64TypeDefinition) {
            ret = uint64Codec((Uint64TypeDefinition) type);
        } else if (type instanceof BooleanTypeDefinition) {
            ret = booleanCodec((BooleanTypeDefinition) type);
        } else if (type instanceof DecimalTypeDefinition) {
            ret = decimalCodec((DecimalTypeDefinition) type);
        } else if (type instanceof EnumTypeDefinition) {
            ret = enumCodec((EnumTypeDefinition) type);
        } else if (type instanceof BitsTypeDefinition) {
            ret = bitsCodec((BitsTypeDefinition) type);
        } else if (type instanceof UnionTypeDefinition) {
            final UnionTypeDefinition union = (UnionTypeDefinition) type;
            if (!isSimpleUnion(union)) {
                return null;
            }
            ret = createSimpleUnion(union);
        } else if (type instanceof BinaryTypeDefinition) {
            ret = binaryCodec((BinaryTypeDefinition) type);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            return instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
        } else {
            return null;
        }

        return cache.getSimple(type, verifyNotNull(ret));
    }

    private static boolean isSimpleUnion(final UnionTypeDefinition union) {
        for (TypeDefinition<?> t : union.getTypes()) {
            if (t instanceof IdentityrefTypeDefinition || t instanceof LeafrefTypeDefinition
                    || t instanceof UnionTypeDefinition && !isSimpleUnion((UnionTypeDefinition) t)) {
                LOG.debug("Type {} has non-simple subtype", t);
                return false;
            }
        }

        LOG.debug("Type {} is simple", union);
        return true;
    }

    private T createComplexCodecFor(final SchemaNode schema, final TypeDefinition<?> type) {
        if (type instanceof UnionTypeDefinition) {
            return createComplexUnion(schema, (UnionTypeDefinition) type);
        } else if (type instanceof LeafrefTypeDefinition) {
            final TypeDefinition<?> target = SchemaContextUtil.getBaseTypeForLeafRef((LeafrefTypeDefinition) type,
                schemaContext, schema);
            verifyNotNull(target, "Unable to find base type for leafref node %s type %s.", schema.getPath(),
                    target);

            final T ret = getSimpleCodecFor(target);
            return ret != null ? ret : createComplexCodecFor(schema, target);
        } else if (type instanceof IdentityrefTypeDefinition) {
            return identityRefCodec((IdentityrefTypeDefinition) type, schema.getQName().getModule());
        } else {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
    }

    private T createSimpleUnion(final UnionTypeDefinition union) {
        final List<TypeDefinition<?>> types = union.getTypes();
        final List<T> codecs = new ArrayList<>(types.size());

        for (TypeDefinition<?> type : types) {
            T codec = cache.lookupSimple(type);
            if (codec == null) {
                codec = verifyNotNull(getSimpleCodecFor(type), "Type %s did not resolve to a simple codec", type);
            }

            codecs.add(codec);
        }

        return unionCodec(union, codecs);
    }

    private T createComplexUnion(final SchemaNode schema, final UnionTypeDefinition union) {
        final List<TypeDefinition<?>> types = union.getTypes();
        final List<T> codecs = new ArrayList<>(types.size());

        for (TypeDefinition<?> type : types) {
            T codec = cache.lookupSimple(type);
            if (codec == null) {
                codec = getSimpleCodecFor(type);
                if (codec == null) {
                    codec = createComplexCodecFor(schema, type);
                }
            }

            codecs.add(verifyNotNull(codec, "Schema %s subtype %s has no codec", schema, type));
        }

        return unionCodec(union, codecs);
    }
}
