/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BitsTypeObject;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

// FIXME: 'SchemaUnawareCodec' is not correct: we use BitsTypeDefinition in construction
final class BitsCodec extends SchemaUnawareCodec {
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
    private static final Cache<Class<? extends BitsTypeObject>, @NonNull BitsCodec> CACHE =
        CacheBuilder.newBuilder().weakKeys().softValues().build();
    private static final MethodType CONSTRUCTOR_INVOKE_TYPE = MethodType.methodType(Object.class, Boolean[].class);

    // Ordered by position
    private final ImmutableMap<String, Method> getters;
    // Ordered by lexical name
    private final ImmutableSet<String> ctorArgs;
    private final MethodHandle ctor;

    private BitsCodec(final MethodHandle ctor, final Set<String> ctorArgs, final Map<String, Method> getters) {
        this.ctor = requireNonNull(ctor);
        this.ctorArgs = ImmutableSet.copyOf(ctorArgs);
        this.getters = ImmutableMap.copyOf(getters);
    }

    static @NonNull BitsCodec of(final Class<?> returnType, final BitsTypeDefinition rootType)
            throws ExecutionException {
        return CACHE.get(returnType.asSubclass(BitsTypeObject.class), () -> {
            final Map<String, Method> getters = new LinkedHashMap<>();
            final Set<String> ctorArgs = new TreeSet<>();

            for (Bit bit : rootType.getBits()) {
                final Method valueGetter = returnType.getMethod(Naming.GETTER_PREFIX
                    + Naming.getClassName(bit.getName()));
                ctorArgs.add(bit.getName());
                getters.put(bit.getName(), valueGetter);
            }
            Constructor<?> constructor = null;
            for (Constructor<?> cst : returnType.getConstructors()) {
                if (!cst.getParameterTypes()[0].equals(returnType)) {
                    constructor = cst;
                }
            }

            final MethodHandle ctor = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .asSpreader(Boolean[].class, ctorArgs.size()).asType(CONSTRUCTOR_INVOKE_TYPE);
            return new BitsCodec(ctor, ctorArgs, getters);
        });
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected Object deserializeImpl(final Object input) {
        checkArgument(input instanceof Set);
        @SuppressWarnings("unchecked")
        final Set<String> casted = (Set<String>) input;

        /*
         * We can do this walk based on field set sorted by name, since constructor arguments in Java Binding are
         * sorted by name.
         *
         * This means we will construct correct array for construction of bits object.
         */
        final Boolean[] args = new Boolean[ctorArgs.size()];
        int currentArg = 0;
        for (String value : ctorArgs) {
            args[currentArg++] = casted.contains(value);
        }

        try {
            return ctor.invokeExact(args);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to instantiate object for " + input, e);
        }
    }

    @Override
    protected Set<String> serializeImpl(final Object input) {
        final Collection<String> result = new ArrayList<>(getters.size());
        for (Entry<String, Method> valueGet : getters.entrySet()) {
            final Boolean value;
            try {
                value = (Boolean) valueGet.getValue().invoke(input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to get bit " + valueGet.getKey(), e);
            }

            if (value) {
                result.add(valueGet.getKey());
            }
        }
        return result.size() == getters.size() ? getters.keySet() : ImmutableSet.copyOf(result);
    }
}
