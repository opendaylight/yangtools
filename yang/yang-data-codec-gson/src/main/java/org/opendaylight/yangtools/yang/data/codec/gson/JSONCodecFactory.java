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
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
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
        public Object deserialize(final String input) {
            return null;
        }

        @Override
        public String serialize(final Object input) {
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

    JSONCodec<?> codecFor(final DataSchemaNode schema) {
        Preconditions.checkArgument(schema instanceof TypedSchemaNode, "Unsupported node type %s", schema.getClass());
        return codecFor((TypedSchemaNode) schema);
    }

    abstract JSONCodec<?> codecFor(final TypedSchemaNode schema);

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
