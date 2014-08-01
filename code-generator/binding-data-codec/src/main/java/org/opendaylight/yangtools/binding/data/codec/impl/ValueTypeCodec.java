/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.concepts.Codec;

/**
 * Value codec, which serializes / deserializes values from DOM simple values.
 *
 */
abstract class ValueTypeCodec implements Codec<Object, Object> {

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

    /**
     *
     * Derived YANG types are just immutable value holders
     * for simple value types, whic are same as in NormalizedNode model.
     *
     */
    static class EncapsulatedValueCodec extends ReflectionBasedCodec {

        private final Method getter;
        private final Constructor<?> constructor;

        public EncapsulatedValueCodec(final Class<?> typeClz) {
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
}
