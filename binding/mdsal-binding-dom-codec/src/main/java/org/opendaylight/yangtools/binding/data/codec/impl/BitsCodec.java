/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.binding.data.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitsCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final MethodType CONSTRUCTOR_INVOKE_TYPE = MethodType.methodType(Object.class, Boolean[].class);
    private final Map<String, Method> valueGetters;
    private final MethodHandle constructor;

    private BitsCodec(final Class<?> typeClass, final SortedMap<String, Method> valueGetters,
            final MethodHandle constructor) {
        super(typeClass);
        this.valueGetters = ImmutableSortedMap.copyOf(valueGetters);
        this.constructor = Preconditions.checkNotNull(constructor);
    }

    static Callable<BitsCodec> loader(final Class<?> returnType, final BitsTypeDefinition rootType) {
        return () -> {
            final SortedMap<String, Method> valueGetters = new TreeMap<>();
            for (Bit bit : rootType.getBits()) {
                final Method valueGetter = returnType.getMethod("is" + BindingMapping.getClassName(bit.getName()));
                valueGetters.put(bit.getName(), valueGetter);
            }
            Constructor<?> constructor = null;
            for (Constructor<?> cst : returnType.getConstructors()) {
                if (!cst.getParameterTypes()[0].equals(returnType)) {
                    constructor = cst;
                }
            }

            final MethodHandle ctor = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .asSpreader(Boolean[].class, valueGetters.size()).asType(CONSTRUCTOR_INVOKE_TYPE);
            return new BitsCodec(returnType, valueGetters, ctor);
        };
    }

    @Override
    public Object deserialize(final Object input) {
        Preconditions.checkArgument(input instanceof Set);
        @SuppressWarnings("unchecked")
        Set<String> casted = (Set<String>) input;

        final Boolean args[] = new Boolean[valueGetters.size()];
        int currentArg = 0;
        /*
         * We can do this walk based on field set
         * sorted by name, since constructor arguments in
         * Java Binding are sorted by name.
         *
         * This means we will construct correct array
         * for construction of bits object.
         */
        for (String value : valueGetters.keySet()) {
            args[currentArg++] = casted.contains(value);
        }

        try {
            return constructor.invokeExact(args);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to instantiate object for " + input, e);
        }
    }

    @Override
    public Object serialize(final Object input) {
        Set<String> result = new HashSet<>();
        for (Entry<String, Method> valueGet : valueGetters.entrySet()) {
            try {
                Boolean value = (Boolean) valueGet.getValue().invoke(input);
                if (value) {
                    result.add(valueGet.getKey());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }
}