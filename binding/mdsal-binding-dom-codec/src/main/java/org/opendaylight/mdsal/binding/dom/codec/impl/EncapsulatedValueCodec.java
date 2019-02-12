/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

/**
 * Derived YANG types are just immutable value holders for simple value
 * types, which are same as in NormalizedNode model.
 */
final class EncapsulatedValueCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final Lookup LOOKUP = MethodHandles.publicLookup();
    private static final MethodType OBJ_METHOD = MethodType.methodType(Object.class, Object.class);
    private final MethodHandle constructor;
    private final MethodHandle getter;
    private final Class<?> valueType;

    private EncapsulatedValueCodec(final Class<?> typeClz, final MethodHandle constructor, final MethodHandle getter,
            final Class<?> valueType) {
        super(typeClz);
        this.constructor = requireNonNull(constructor);
        this.getter = requireNonNull(getter);
        this.valueType = requireNonNull(valueType);
    }

    static Callable<EncapsulatedValueCodec> loader(final Class<?> typeClz, TypeDefinition<?> typeDef) {
        return () -> {
            final Method m;
            if (typeDef instanceof BooleanTypeDefinition || typeDef instanceof EmptyTypeDefinition) {
                m = typeClz.getMethod("isValue");
            } else {
                m = typeClz.getMethod("getValue");
            }
            final MethodHandle getter = LOOKUP.unreflect(m).asType(OBJ_METHOD);
            final Class<?> valueType = m.getReturnType();

            final MethodHandle constructor = LOOKUP.findConstructor(typeClz,
                MethodType.methodType(void.class, valueType)).asType(OBJ_METHOD);
            return new EncapsulatedValueCodec(typeClz, constructor, getter, valueType);
        };
    }

    /**
     * Quick check if a value object has a chance to deserialize using {@link #deserialize(Object)}.
     *
     * @param value Value to be checked
     * @return True if the value can be encapsulated
     */
    boolean canAcceptObject(final Object value) {
        return valueType.isInstance(value);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object deserialize(final Object input) {
        try {
            return constructor.invokeExact(input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object serialize(final Object input) {
        try {
            return getter.invokeExact(input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}