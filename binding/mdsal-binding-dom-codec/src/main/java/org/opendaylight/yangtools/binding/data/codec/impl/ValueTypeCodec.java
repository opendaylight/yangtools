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
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Value codec, which serializes / deserializes values from DOM simple values.
 *
 */
abstract class ValueTypeCodec implements Codec<Object, Object> {

    private static final Cache<Class<?>, SchemaUnawareCodec> staticCodecs = CacheBuilder.newBuilder().weakKeys()
            .build();


    /**
     * Marker interface for codecs, which functionality will not be
     * affected by schema change (introduction of new YANG modules)
     * they may have one static instance generated when
     * first time needed.
     *
     */
    interface SchemaUnawareCodec extends Codec<Object,Object> {

    }


    /**
     *
     * No-op Codec, Java YANG Binding uses same types as NormalizedNode model
     * for base YANG types, representing numbers, binary and strings.
     *
     *
     */
    public static final SchemaUnawareCodec NOOP_CODEC = new SchemaUnawareCodec() {

        @Override
        public Object serialize(final Object input) {
            return input;
        }

        @Override
        public Object deserialize(final Object input) {
            return input;
        }
    };

    public static final SchemaUnawareCodec EMPTY_CODEC = new SchemaUnawareCodec() {

        @Override
        public Object serialize(final Object arg0) {
            // Empty type has null value in NormalizedNode and Composite Node
            // representation
            return null;
        }

        @Override
        public Object deserialize(final Object arg0) {
            /* Empty type has boolean.TRUE representation in Binding-aware world
            *  otherwise it is null / false.
            *  So when codec is triggered, empty leaf is present, that means we
            *  are safe to return true.
            */
            return Boolean.TRUE;
        }
    };

    private static final Callable<? extends SchemaUnawareCodec> EMPTY_LOADER = new Callable<SchemaUnawareCodec>() {

        @Override
        public SchemaUnawareCodec call() {
            return EMPTY_CODEC;
        }
    };


    public static SchemaUnawareCodec getCodecFor(final Class<?> typeClz, final TypeDefinition<?> def) {
        if (BindingReflections.isBindingClass(typeClz)) {
            return getCachedSchemaUnawareCodec(typeClz, getCodecLoader(typeClz, def));
        }
        return def instanceof EmptyTypeDefinition ? EMPTY_CODEC : NOOP_CODEC;
    }

    private static SchemaUnawareCodec getCachedSchemaUnawareCodec(final Class<?> typeClz, final Callable<? extends SchemaUnawareCodec> loader) {
        try {
            return staticCodecs.get(typeClz, loader);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Callable<? extends SchemaUnawareCodec> getCodecLoader(final Class<?> typeClz, final TypeDefinition<?> def) {

        TypeDefinition<?> rootType = def;
        while (rootType.getBaseType() != null) {
            rootType = rootType.getBaseType();
        }
        if (rootType instanceof EnumTypeDefinition) {
            return EnumerationCodec.loader(typeClz, (EnumTypeDefinition) rootType);
        } else if (rootType instanceof BitsTypeDefinition) {
            return BitsCodec.loader(typeClz, (BitsTypeDefinition) rootType);
        } else if (rootType instanceof EmptyTypeDefinition) {
            return EMPTY_LOADER;
        }
        return EncapsulatedValueCodec.loader(typeClz);
    }

    @SuppressWarnings("rawtypes")
    static ValueTypeCodec encapsulatedValueCodecFor(final Class<?> typeClz, final Codec delegate) {
        SchemaUnawareCodec extractor = getCachedSchemaUnawareCodec(typeClz, EncapsulatedValueCodec.loader(typeClz));
        return new CompositeValueCodec(extractor, delegate);
    }

}
