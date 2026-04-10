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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;

/**
 * A factory providing {@link UnsafeScalarTypeObjectFactory} instances.
 */
@NonNullByDefault
@FunctionalInterface
interface UnsafeSTOFactoryFactory {

    <V, T extends ScalarTypeObject<V>> UnsafeScalarTypeObjectFactory<V, T> factoryFor(Class<T> clazz,
        Function<V, T> safeCtor, BiFunction<UnsafeSecret, V, T> unsafeCtor);
}
