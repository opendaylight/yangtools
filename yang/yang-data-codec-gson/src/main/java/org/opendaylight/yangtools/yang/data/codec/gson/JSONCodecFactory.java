/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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
 */
@Beta
public abstract class JSONCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JSONCodecFactory.class);
    private static final JSONCodec<Object> NULL_CODEC = new JSONCodec<Object>() {
        @Override
        public Class<Object> getDataClass() {
            return Object.class;
        }

        @Override
        public Object deserializeString(final String value) {
            LOG.warn("Call of the deserializeString method on JSONCodecFactory.NULL_CODEC object. No operation performed.");
            return null;
        }

        @Override
        public void serializeToWriter(final JsonWriter writer, final Object value) throws IOException {
            // NOOP since codec is unkwown.
            LOG.warn("Call of the serializeToWriter method on JSONCodecFactory.NULL_CODEC object. No operation performed.");
        }
    };

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
     */
    public static JSONCodecFactory create(final SchemaContext context) {
        return SharedJSONCodecFactory.get(context);
    }

    final JSONCodec<?> codecFor(final DataSchemaNode schema) {
        Preconditions.checkArgument(schema instanceof TypedSchemaNode, "Unsupported node type %s", schema.getClass());
        return getCodecFor((TypedSchemaNode) schema);
    }

    private JSONCodec<?> getCodecFor(final TypedSchemaNode schema) {
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
            return ret;
        }
        ret = lookupComplex(schema);
        if (ret != null) {
            return ret;
        }

        // Dealing with simple types...
        ret = getSimpleCodecFor(type);
        if (ret != null) {
            return ret;
        }

        // Now deal with the pesky complex types
        if (type instanceof LeafrefTypeDefinition) {
            ret = createReferencedTypeCodec(schema, (LeafrefTypeDefinition) type);
        } else if (type instanceof IdentityrefTypeDefinition) {
            ret = new JSONStringIdentityrefCodec(schemaContext, schema.getQName().getModule());
        } else if (type instanceof UnionTypeDefinition) {
            ret =  createUnionTypeCodec(schema, (UnionTypeDefinition) type);
        }

        Preconditions.checkArgument(ret != null, "Unsupported type %s", type);
        return getComplex(schema, ret);
    }

    JSONCodec<?> simpleCodecFor(final TypeDefinition<?> type) {
        final JSONCodec<?> ret = lookupSimple(type);
        return ret != null ? ret : Verify.verifyNotNull(getSimpleCodecFor(type),
            "Type %s did not resolve to a simple codec", type);
    }

    private JSONCodec<?> getSimpleCodecFor(final TypeDefinition<?> type) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
            // FIXME: there really are two favors, as 'require-instance true' needs to be validated. In order to deal
            //        with that, though, we need access to the current data store.
            return iidCodec;
        } else if (type instanceof EmptyTypeDefinition) {
            return JSONEmptyCodec.INSTANCE;
        } else if (type instanceof UnknownTypeDefinition) {
            return NULL_CODEC;
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
            ret = isSimpleUnion(union) ? SimpleUnionJSONCodec.create(this, union) : null;
        } else if (type instanceof BinaryTypeDefinition) {
            ret = new QuotedJSONCodec<>(BinaryStringCodec.from((BinaryTypeDefinition) type));
        } else {
            return null;
        }

        return ret == null ? ret : getSimple(type, ret);
    }

    private static boolean isSimpleUnion(final UnionTypeDefinition union) {
        for (TypeDefinition<?> t : union.getTypes()) {
            if (t instanceof IdentityrefTypeDefinition || t instanceof LeafrefTypeDefinition) {
                return false;
            }
            if (t instanceof UnionTypeDefinition && !isSimpleUnion((UnionTypeDefinition) t)) {
                return false;
            }
        }

        return true;
    }

    abstract JSONCodec<?> lookupComplex(TypedSchemaNode schema);

    abstract JSONCodec<?> lookupSimple(TypeDefinition<?> type);

    abstract JSONCodec<?> getComplex(TypedSchemaNode schema, JSONCodec<?> codec);

    abstract JSONCodec<?> getSimple(TypeDefinition<?> type, JSONCodec<?> codec);

    final JSONCodec<?> createCodec(final DataSchemaNode key, final TypeDefinition<?> type) {
        if (type instanceof LeafrefTypeDefinition) {
            return createReferencedTypeCodec(key, (LeafrefTypeDefinition) type);
        } else if (type instanceof IdentityrefTypeDefinition) {
            return new JSONStringIdentityrefCodec(schemaContext, key.getQName().getModule());
        } else if (type instanceof UnionTypeDefinition) {
            return createUnionTypeCodec(key, (UnionTypeDefinition) type);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            return iidCodec;
        } else if (type instanceof EmptyTypeDefinition) {
            return JSONEmptyCodec.INSTANCE;
        }

        final TypeDefinitionAwareCodec<Object, ?> codec = TypeDefinitionAwareCodec.from(type);
        if (codec == null) {
            // catches anyxml
            LOG.debug("Codec for {} is not implemented yet", type);
            return NULL_CODEC;
        }
        return AbstractJSONCodec.create(codec);
    }

    final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    final JSONCodec<?> codecFor(final DataSchemaNode schema, final TypeDefinition<?> unionSubType) {
        return createCodec(schema, unionSubType);
    }

    private JSONCodec<?> createReferencedTypeCodec(final DataSchemaNode schema, final LeafrefTypeDefinition type) {
        // FIXME: Verify if this does indeed support leafref of leafref
        final TypeDefinition<?> referencedType = SchemaContextUtil.getBaseTypeForLeafRef(type, schemaContext, schema);
        Verify.verifyNotNull(referencedType, "Unable to find base type for leafref node '%s'.", schema.getPath());
        return createCodec(schema, referencedType);
    }

    private JSONCodec<Object> createUnionTypeCodec(final DataSchemaNode schema, final UnionTypeDefinition type) {
        final JSONCodec<Object> jsonStringUnionCodec = new JSONStringUnionCodec(schema, type, this);
        return jsonStringUnionCodec;
    }


}
