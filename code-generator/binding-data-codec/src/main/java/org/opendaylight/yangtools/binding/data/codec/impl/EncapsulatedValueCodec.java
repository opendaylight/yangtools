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
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.binding.data.codec.impl.ValueTypeCodec.SchemaUnawareCodec;

/**
 *
 * Derived YANG types are just immutable value holders for simple value
 * types, which are same as in NormalizedNode model.
 *
 */
class EncapsulatedValueCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {

    private final Method getter;
    private final Constructor<?> constructor;

    EncapsulatedValueCodec(final Class<?> typeClz) {
        super(typeClz);
        try {
            this.getter = typeClz.getMethod("getValue");
            this.constructor = typeClz.getConstructor(getter.getReturnType());
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Could not resolve required method.", e);
        }
    }

    static Callable<EncapsulatedValueCodec> loader(final Class<?> typeClz) {
        return new Callable<EncapsulatedValueCodec>() {
            @Override
            public EncapsulatedValueCodec call() throws Exception {
                return new EncapsulatedValueCodec(typeClz);
            }
        };
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