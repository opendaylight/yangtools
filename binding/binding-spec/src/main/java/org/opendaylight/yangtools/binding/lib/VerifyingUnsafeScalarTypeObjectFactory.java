/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;

@NonNullByDefault
record VerifyingUnsafeScalarTypeObjectFactory<T extends ScalarTypeObject<V>, V>(Class<T> target, Function<V, T> ctor)
        implements UnsafeScalarTypeObjectFactory<T, V> {
    VerifyingUnsafeScalarTypeObjectFactory {
        requireNonNull(target);
        requireNonNull(ctor);
    }

    @Override
    public T newInstance(final V value) {
        return ctor.apply(value);
    }
}
