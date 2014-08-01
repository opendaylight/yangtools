/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableBiMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Value codec, which serializes / deserializes values from DOM simple values.
 *
 */
abstract class ValueTypeCodec implements Codec<Object, Object> {

    private static final LoadingCache<Class<?>,EncapsulatedValueCodec> encapsulatedCodecs = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<Class<?>,EncapsulatedValueCodec>() {

                @Override
                public EncapsulatedValueCodec load(final Class<?> key) throws Exception {
                    return new EncapsulatedValueCodec(key);
                }
            });

    private static final Cache<Class<?>,EnumerationCodec> enumerationCodecs = CacheBuilder.newBuilder().weakKeys().build();

    /**
     *
     * No-op Codec, Java YANG Binding uses same types
     * as NormalizedNode model for base YANG types, representing
     * numbers, binary and strings.
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

    abstract static class ReflectionBasedCodec extends ValueTypeCodec {

        protected final Class<?> typeClass;

        public ReflectionBasedCodec(final Class<?> typeClass) {
            super();
            this.typeClass = typeClass;
        }

    }

    public static ValueTypeCodec encapsulatedValueCodecFor(final Class<?> typeClz) {
        return encapsulatedCodecs.getUnchecked(typeClz);
    }

    public static ValueTypeCodec encapsulatedValueCodecFor(final Class<?> typeClz,final Codec delegate) {
        EncapsulatedValueCodec extractor = encapsulatedCodecs.getUnchecked(typeClz);
        return new CompositeValueCodec(extractor,delegate);
    }

    /**
     *
     * Derived YANG types are just immutable value holders
     * for simple value types, whic are same as in NormalizedNode model.
     *
     */
    static class EncapsulatedValueCodec extends ReflectionBasedCodec {

        private final Method getter;
        private final Constructor<?> constructor;

        private EncapsulatedValueCodec(final Class<?> typeClz) {
            super(typeClz);
            try {
                this.getter = typeClz.getMethod("getValue");
                this.constructor = typeClz.getConstructor(getter.getReturnType());
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Could not resolve required method.", e);
            }
        }

        @Override
        public Object deserialize(final Object input) {
            try {
                return constructor.newInstance(input);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object serialize(final Object input) {
            try {
                return getter.invoke(input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class CompositeValueCodec extends ValueTypeCodec {

        private final EncapsulatedValueCodec bindingToSimpleType;
        private final Codec bindingToDom;

        public CompositeValueCodec(final EncapsulatedValueCodec extractor, final Codec delegate) {
            this.bindingToSimpleType = extractor;
            this.bindingToDom = delegate;
        }

        @Override
        public Object deserialize(final Object input) {
            return bindingToSimpleType.deserialize(bindingToDom.deserialize(input));
        }

        @Override
        public Object serialize(final Object input) {
            return bindingToDom.serialize(bindingToSimpleType.serialize(input));
        }

    }

    static class EnumerationCodec extends ValueTypeCodec {

        ImmutableBiMap<String,Enum<?>> yangValueToBinding;
        private final Class<? extends Enum<?>> enumClass;

        public EnumerationCodec(final Class<? extends Enum<?>> enumeration,final Map<String,Enum<?>> schema) {
            yangValueToBinding = ImmutableBiMap.copyOf(schema);
            enumClass = enumeration;

        }

        @Override
        public Object deserialize(final Object input) {
            Enum<?> value = yangValueToBinding.get(input);
            Preconditions.checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s",input,yangValueToBinding.keySet());
            return value;
        }

        @Override
        public Object serialize(final Object input) {
            Preconditions.checkArgument(enumClass.isInstance(input), "Input must be instance of %s",enumClass);
            return yangValueToBinding.inverse().get(input);
        }
    }

    public static Codec<Object, Object> enumerationCodec(final Class<?> returnType, final EnumTypeDefinition enumSchema) {
        Preconditions.checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Enum<?>> enumType = (Class) returnType;
        try {
            return enumerationCodecs.get(returnType, new Callable<EnumerationCodec>() {
                @Override
                public EnumerationCodec call() throws Exception {


                    Map<String,Enum<?>> nameToValue = new HashMap<>();
                    for(Enum<?> enumValue : enumType.getEnumConstants()) {
                        nameToValue.put(enumValue.toString(),enumValue);
                    }
                    Map<String,Enum<?>> yangNameToBinding = new HashMap<>();
                    for(EnumPair yangValue : enumSchema.getValues()) {
                        final String bindingName = BindingMapping.getClassName(yangValue.getName());
                        final Enum<?> bindingVal = nameToValue.get(bindingName);
                        yangNameToBinding.put(yangValue.getName(), bindingVal);
                    }
                    return new EnumerationCodec(enumType, yangNameToBinding);
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Could not load enumeration codec for " + returnType,e);
        }
    }
}
