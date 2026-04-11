/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;

/**
 * Default implementation of {@link ScalarTypeObjectRegistrar}. Hidden on purpose: we give these out paired with
 * {@link DefaultUnsafeAccess}.
 */
// TODO: value record when we have JEP-401 available
@NonNullByDefault
record DefaultSTORegistrar(UnsafeAccessState state) implements ScalarTypeObjectRegistrar {
    DefaultSTORegistrar {
        requireNonNull(state);
    }

    /**
     * Register a {@link ScalarTypeObject} capable of unsafe construction. There is no facility for unregistration, as
     * this method is meant to be invoked during a ScalarTypeObject's class initialization.
     *
     * @param <V> the value type
     * @param <T> the {@link ScalarTypeObject} type
     * @param typeClass the {@link ScalarTypeObject} class
     * @param safeCtor the safe constructor
     * @param unsafeCtor the unsafe constructor
     */
    @Override
    public <V, T extends ScalarTypeObject<V>> void registerUnsafeSTO(final Class<T> typeClass,
            final Function<V, T> safeCtor, final BiFunction<UnsafeSecret, V, T> unsafeCtor) {
        if (!ScalarTypeObject.class.isAssignableFrom(typeClass)) {
            throw new IllegalArgumentException(typeClass + " is not a ScalarTypeObject");
        }
        state.putSTO(UnsafeAccessSupport.STO_FACTORY_FACTORY.factoryFor(typeClass,
            requireNonNull(safeCtor), requireNonNull(unsafeCtor)));
    }

    @Override
    public String toString() {
        return state.computeToString(ScalarTypeObjectRegistrar.class);
    }
}
