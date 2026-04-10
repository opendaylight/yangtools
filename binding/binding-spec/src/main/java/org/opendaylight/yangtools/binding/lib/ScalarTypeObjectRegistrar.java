/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Interface exposed for generated {@link ScalarTypeObject} classes. Those that perform validation on their value object
 * register with a module-global instance of this interface, so that their unsafe constructors can be accessed
 * via {@link UnsafeAccess}.
 */
public sealed interface ScalarTypeObjectRegistrar extends Mutable permits DefaultSTORegistrar {
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
    <V, T extends ScalarTypeObject<V>> void registerUnsafeSTO(@NonNull Class<T> typeClass,
        @NonNull Function<V, T> safeCtor, @NonNull BiFunction<UnsafeSecret, V, T> unsafeCtor);
}
