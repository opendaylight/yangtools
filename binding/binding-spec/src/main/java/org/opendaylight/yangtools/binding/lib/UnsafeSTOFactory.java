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
import org.opendaylight.yangtools.binding.impl.TheUnsafeSecret;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;

@NonNullByDefault
record UnsafeSTOFactory<V, T extends ScalarTypeObject<V>>(Class<T> target, BiFunction<UnsafeSecret, V, T> ctor)
        implements UnsafeScalarTypeObjectFactory<V, T> {

    static final UnsafeSTOFactoryFactory FACTORY = UnsafeSTOFactory::new;

    private UnsafeSTOFactory(final Class<T> clazz, final Function<V, T> safeCtor,
            final BiFunction<UnsafeSecret, V, T> unsafeCtor) {
        this(clazz, unsafeCtor);
    }

    UnsafeSTOFactory {
        requireNonNull(target);
        requireNonNull(ctor);
    }

    @Override
    public T newInstance(final V value) {
        return ctor.apply(TheUnsafeSecret.INSTANCE, value);
    }
}
