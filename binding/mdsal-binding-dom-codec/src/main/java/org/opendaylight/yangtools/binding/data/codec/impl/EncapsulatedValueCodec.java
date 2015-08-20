/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.binding.data.codec.impl.ValueTypeCodec.SchemaUnawareCodec;

/**
 *
 * Derived YANG types are just immutable value holders for simple value
 * types, which are same as in NormalizedNode model.
 *
 */
final class EncapsulatedValueCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final Lookup LOOKUP = MethodHandles.publicLookup();
    private static final MethodType OBJ_METHOD = MethodType.methodType(Object.class, Object.class);
    private final MethodHandle constructor;
    private final MethodHandle getter;

    private EncapsulatedValueCodec(final Class<?> typeClz, final MethodHandle constructor, final MethodHandle getter) {
        super(typeClz);
        this.constructor = Preconditions.checkNotNull(constructor);
        this.getter = Preconditions.checkNotNull(getter);
    }

    static Callable<EncapsulatedValueCodec> loader(final Class<?> typeClz) {
        return new Callable<EncapsulatedValueCodec>() {
            @Override
            public EncapsulatedValueCodec call() throws Exception {
                final Method m = typeClz.getMethod("getValue");
                final MethodHandle getter = LOOKUP.unreflect(m).asType(OBJ_METHOD);
                final MethodHandle constructor = LOOKUP.findConstructor(typeClz, MethodType.methodType(void.class, m.getReturnType())).asType(OBJ_METHOD);
                return new EncapsulatedValueCodec(typeClz, constructor, getter);
            }
        };
    }

    @Override
    public Object deserialize(final Object input) {
        try {
            return constructor.invokeExact(input);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Object serialize(final Object input) {
        try {
            return getter.invokeExact(input);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}