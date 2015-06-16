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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DerivedType;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link SchemaContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
@Beta
public final class JSONCodecFactory {
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
        public boolean needQuotes() {
            return false;
        }

        @Override
        public void serializeToWriter(final JsonWriter writer, final Object value) throws IOException {
            // NOOP since codec is unkwown.
            LOG.warn("Call of the serializeToWriter method on JSONCodecFactory.NULL_CODEC object. No operation performed.");
        }
    };

    private final LoadingCache<DataSchemaNode, JSONCodec<Object>> codecs =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<DataSchemaNode, JSONCodec<Object>>() {
        @Override
        public JSONCodec<Object> load(final DataSchemaNode key) throws Exception {
            final TypeDefinition<?> type;
            if (key instanceof LeafSchemaNode) {
                type = ((LeafSchemaNode) key).getType();
            } else if (key instanceof LeafListSchemaNode) {
                type = ((LeafListSchemaNode) key).getType();
            } else {
                throw new IllegalArgumentException("Not supported node type " + key.getClass().getName());
            }
            return createCodec(key,type);
        }
    });

    private final SchemaContext schemaContext;
    private final JSONCodec<?> iidCodec;

    private JSONCodecFactory(final SchemaContext context) {
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
        return new JSONCodecFactory(context);
    }

    @SuppressWarnings("unchecked")
    private JSONCodec<Object> createCodec(final DataSchemaNode key, final TypeDefinition<?> type) {
        final TypeDefinition<?> normalizedType = DerivedType.from(type);
        if (normalizedType instanceof LeafrefTypeDefinition) {
            return createReferencedTypeCodec(key, (LeafrefTypeDefinition) normalizedType);
        } else if (normalizedType instanceof IdentityrefTypeDefinition) {
            final JSONCodec<?> jsonStringIdentityrefCodec =
                    new JSONStringIdentityrefCodec(schemaContext, key.getQName().getModule());
            return (JSONCodec<Object>) jsonStringIdentityrefCodec;
        }
        return createFromSimpleType(normalizedType);
    }

    private JSONCodec<Object> createReferencedTypeCodec(final DataSchemaNode schema,
            final LeafrefTypeDefinition type) {
        // FIXME: Verify if this does indeed support leafref of leafref
        final TypeDefinition<?> referencedType =
                SchemaContextUtil.getBaseTypeForLeafRef(type, getSchemaContext(), schema);
        return createCodec(schema, referencedType);
    }

    @SuppressWarnings("unchecked")
    private JSONCodec<Object> createFromSimpleType(final TypeDefinition<?> type) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
            return (JSONCodec<Object>) iidCodec;
        }
        if (type instanceof EmptyTypeDefinition) {
            return (JSONCodec<Object>) JSONEmptyCodec.INSTANCE;
        }

        final TypeDefinitionAwareCodec<Object, ?> codec = TypeDefinitionAwareCodec.from(type);
        if (codec == null) {
            LOG.debug("Codec for type \"{}\" is not implemented yet.", type.getQName()
                    .getLocalName());
            return NULL_CODEC;
        }
        return (JSONCodec<Object>) AbstractJSONCodec.create(codec);
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    JSONCodec<Object> codecFor(final DataSchemaNode schema) {
        return codecs.getUnchecked(schema);
    }

}
