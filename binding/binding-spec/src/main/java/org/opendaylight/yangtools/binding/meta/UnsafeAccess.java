/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import java.util.NoSuchElementException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;

/**
 * Unsafe access to various aspects of binding code.
 *
 * @since 15.0.3
 */
public interface UnsafeAccess {
    /**
     * {@return the {@link UnsafeScalarTypeObjectFactory} for the specified {@code typeClass}, or {@code null} if not
     * found}
     * @param <T> the {@link ScalarTypeObject} type
     * @param <V> the value type
     * @param typeObj an instance of the {@link ScalarTypeObject} class
     * @throws IllegalArgumentException if the specified class is not appropriate for this access
     */
    <T extends ScalarTypeObject<V>, V>
        @Nullable UnsafeScalarTypeObjectFactory<T, V> lookupUnsafeScalarTypeObjectFactory(@NonNull T typeObj);

    /**
     * {@return the {@link UnsafeScalarTypeObjectFactory} for the specified {@code typeClass}}
     * @param <T> the {@link ScalarTypeObject} type
     * @param <V> the value type
     * @param typeObj an instance of the {@link ScalarTypeObject} class
     * @throws IllegalArgumentException if the specified class is not appropriate for this access
     * @throws NoSuchElementException if the factory cannot be found
     */
    default <T extends ScalarTypeObject<V>, V>
            @NonNull UnsafeScalarTypeObjectFactory<T, V> getUnsafeScalarTypeObjectFactory(final @NonNull T typeObj) {
        final var result = lookupUnsafeScalarTypeObjectFactory(typeObj);
        if (result == null) {
            throw new NoSuchElementException("Factory for " + typeObj.getClass().getCanonicalName() + " not found");
        }
        return result;
    }
}
