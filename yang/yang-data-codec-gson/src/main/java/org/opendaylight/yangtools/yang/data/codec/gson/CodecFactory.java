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

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.codec.LeafrefCodec;
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
    private static final Codec<?, ?> LEAFREF_DEFAULT_CODEC = new LeafrefCodec<String>() {
        @Override
        public String serialize(final Object data) {
            return String.valueOf(data);
        }

        @Override
        public Object deserialize(final String data) {
            return data;
        }
    };
    private static final Codec<?, ?> NULL_CODEC = new Codec<Object, Object>() {
        @Override
        public Object deserialize(final Object input) {
            return null;
        }

        @Override
        public Object serialize(final Object input) {
            return null;
        }
    };


    private static TypeDefinition<?> resolveBaseTypeFrom(final TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }

    private final LoadingCache<TypeDefinition<?>, Codec<?, ?>> codecs =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<TypeDefinition<?>, Codec<?, ?>>() {
        @Override
        public Codec<?, ?> load(final TypeDefinition<?> key) throws Exception {
            final TypeDefinition<?> type = resolveBaseTypeFrom(key);

            if (type instanceof InstanceIdentifierType) {
                return iidCodec;
            }
            if (type instanceof IdentityrefType) {
                return idrefCodec;
            }
            if (type instanceof LeafrefTypeDefinition) {
                return LEAFREF_DEFAULT_CODEC;
            }

            final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = TypeDefinitionAwareCodec.from(type);
            if (codec == null) {
                LOG.debug("Codec for type \"{}\" is not implemented yet.", type.getQName().getLocalName());
                return NULL_CODEC;
            }

            return codec;
        }
    });

    private final Codec<?, ?> iidCodec;
    private final Codec<?, ?> idrefCodec;

    private CodecFactory(final SchemaContext context) {
        iidCodec = new JSONStringInstanceIdentifierCodec(context);
        idrefCodec = new JSONStringIdentityrefCodec(context);
    }

    public static CodecFactory create(final SchemaContext context) {
        return new CodecFactory(context);
    }

    @SuppressWarnings("unchecked")
    public final Codec<Object, Object> codecFor(final TypeDefinition<?> typeDefinition) {
        return (Codec<Object, Object>) codecs.getUnchecked(typeDefinition);
    }
}
