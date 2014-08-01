/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Value codec, which serializes / deserializes values from DOM simple values.
 *
 */
abstract class ValueTypeCodec implements Codec<Object, Object> {

    private static final Cache<Class<?>, ReflectionBasedCodec> REFLECTION_CODECS = CacheBuilder.newBuilder().weakKeys()
            .build();

    /**
     *
     * No-op Codec, Java YANG Binding uses same types as NormalizedNode model
     * for base YANG types, representing numbers, binary and strings.
     *
     *
     */
    public static final ValueTypeCodec NOOP_CODEC = new ValueTypeCodec() {

        @Override
        public Object serialize(final Object input) {
            return input;
        }

        @Override
        public Object deserialize(final Object input) {
            return input;
        }
    };

    public static ValueTypeCodec getCodecFor(final Class<?> typeClz, final TypeDefinition<?> def) {
        if (BindingReflections.isBindingClass(typeClz)) {
            return getReflectionCodec(typeClz, getCodecLoader(typeClz, def));
        }
        return NOOP_CODEC;
    }

    private static ValueTypeCodec getReflectionCodec(final Class<?> typeClz, final Callable<ReflectionBasedCodec> loader) {
        try {
            return REFLECTION_CODECS.get(typeClz, loader);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Callable<ReflectionBasedCodec> getCodecLoader(final Class<?> typeClz, final TypeDefinition<?> def) {

        TypeDefinition<?> rootType = def;
        while (rootType.getBaseType() != null) {
            rootType = rootType.getBaseType();
        }
        if (rootType instanceof EnumTypeDefinition) {
            return EnumerationCodec.loader(typeClz, (EnumTypeDefinition) rootType);
        } else if (rootType instanceof BitsTypeDefinition) {
            return BitsCodec.loader(typeClz, (BitsTypeDefinition) rootType);
        }
        return EncapsulatedValueCodec.loader(typeClz);
    }

    @SuppressWarnings("rawtypes")
    static ValueTypeCodec encapsulatedValueCodecFor(final Class<?> typeClz, final Codec delegate) {
        ValueTypeCodec extractor = getReflectionCodec(typeClz, EncapsulatedValueCodec.loader(typeClz));
        return new CompositeValueCodec(extractor, delegate);
    }


}
