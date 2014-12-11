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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link SchemaContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
@Beta
public final class JSONCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JSONCodecFactory.class);
    private static final JSONCodec<Object> LEAFREF_DEFAULT_CODEC = new JSONLeafrefCodec();
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
        public void serializeToWriter(JsonWriter writer, Object value) throws IOException {
            // NOOP since codec is unkwown.

        }
    };

    private static TypeDefinition<?> resolveBaseTypeFrom(final TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }

    private final LoadingCache<TypeDefinition<?>, JSONCodec<Object>> codecs =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<TypeDefinition<?>, JSONCodec<Object>>() {
        @SuppressWarnings("unchecked")
        @Override
        public JSONCodec<Object> load(final TypeDefinition<?> key) throws Exception {
            final TypeDefinition<?> type = resolveBaseTypeFrom(key);

            if (type instanceof InstanceIdentifierType) {
                return (JSONCodec<Object>) iidCodec;
            }
            if (type instanceof IdentityrefType) {
                return (JSONCodec<Object>) idrefCodec;
            }
            if (type instanceof LeafrefTypeDefinition) {
                return LEAFREF_DEFAULT_CODEC;
            }

            final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = TypeDefinitionAwareCodec.from(type);
            if (codec == null) {
                LOG.debug("Codec for type \"{}\" is not implemented yet.", type.getQName().getLocalName());
                return NULL_CODEC;
            }

            return (JSONCodec<Object>) AbstractJSONCodec.create(codec);
        }
    });

    private final SchemaContext schemaContext;
    private final JSONCodec<?> iidCodec;
    private final JSONCodec<?> idrefCodec;

    private JSONCodecFactory(final SchemaContext context) {
        this.schemaContext = Preconditions.checkNotNull(context);
        iidCodec = new JSONStringInstanceIdentifierCodec(context);
        idrefCodec = new JSONStringIdentityrefCodec(context);
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

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    JSONCodec<Object> codecFor(final TypeDefinition<?> typeDefinition) {
        return codecs.getUnchecked(typeDefinition);
    }
}
