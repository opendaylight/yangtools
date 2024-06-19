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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

/**
 * Derived YANG types are just immutable value holders for simple value
 * types, which are same as in NormalizedNode model.
 */
final class EncapsulatedValueCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final MethodType OBJ_METHOD = MethodType.methodType(Object.class, Object.class);

    /*
     * Use identity comparison for keys and allow classes to be GCd themselves.
     *
     * Since codecs can (and typically do) hold a direct or indirect strong reference to the class, they need to be also
     * accessed via reference. Using a weak reference could be problematic, because the codec would quite often be only
     * weakly reachable. We therefore use a soft reference, whose implementation guidance is suitable to our use case:
     *
     *     "Virtual machine implementations are, however, encouraged to bias against clearing recently-created or
     *      recently-used soft references."
     */
    private static final LoadingCache<Class<?>, EncapsulatedValueCodec> CACHE = CacheBuilder.newBuilder().weakKeys()
        .softValues().build(new CacheLoader<>() {
            @Override
            public EncapsulatedValueCodec load(final Class<?> key) throws ReflectiveOperationException {
                final Method m = key.getMethod(BindingMapping.SCALAR_TYPE_OBJECT_GET_VALUE_NAME);
                final Lookup lookup = MethodHandles.publicLookup();
                final MethodHandle getter = lookup.unreflect(m).asType(OBJ_METHOD);
                final Class<?> valueType = m.getReturnType();
                final MethodHandle constructor = lookup.findConstructor(key,
                    MethodType.methodType(void.class, valueType)).asType(OBJ_METHOD);
                return new EncapsulatedValueCodec(key, constructor, getter, valueType);
            }
        });

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

    static @NonNull EncapsulatedValueCodec of(final Class<?> typeClz) throws ExecutionException {
        return CACHE.get(typeClz);
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
            throw new IllegalStateException(e);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object serialize(final Object input) {
        try {
            return getter.invokeExact(input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }
}