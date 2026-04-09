/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;

/**
 * An factory capable of giving out instances of a {@link ScalarTypeObject} type which do not validate their value.
 *
 * @since 15.0.3
 */
public interface UnsafeScalarTypeObjectFactory<T extends ScalarTypeObject<V>, V> {
    /**
     * {@return the target {@link ScalarTypeObject} class}
     */
    @NonNullByDefault
    Class<T> target();

    /**
     * {@return a new instance of target {@link ScalarTypeObject} with specified value}
     * @param value the value
     */
    @NonNullByDefault
    T newInstance(V value);
}
