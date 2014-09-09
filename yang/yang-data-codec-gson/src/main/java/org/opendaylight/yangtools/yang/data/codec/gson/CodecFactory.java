/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation-internal and subject to change. Please do not use it.
 */
@Beta
final class CodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CodecFactory.class);
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

            return AbstractJSONCodec.create(codec);
        }
    });

    private final JSONCodec<?> iidCodec;
    private final JSONCodec<?> idrefCodec;

    private CodecFactory(final SchemaContext context) {
        iidCodec = new JSONStringInstanceIdentifierCodec(context);
        idrefCodec = new JSONStringIdentityrefCodec(context);
    }

    public static CodecFactory create(final SchemaContext context) {
        return new CodecFactory(context);
    }

    public final JSONCodec<Object> codecFor(final TypeDefinition<?> typeDefinition) {
        return codecs.getUnchecked(typeDefinition);
    }
}
