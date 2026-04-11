/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Abstract base class for {@link DataContainer} implementations. It implements {@link #hashCode()} caching and common
 * handling of {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <T> the {@link DataContainer} type
 * @since 15.1.0
 */
public abstract class AbstractDataContainer<T extends DataContainer> {
    // TODO: single field when hashCode() is defined to be != 0
    private int hashCode;
    private volatile boolean hashCodeValid;

    /**
     * {@return the equality class, should always be the same as {@link BindingContract#implementedInterface()}}
     */
    protected abstract @NonNull Class<T> equalityClass();

    @Override
    public final int hashCode() {
        return hashCodeValid ? hashCode : loadHashCode();
    }

    private int loadHashCode() {
        final var result = computeHashCode();
        hashCode = result;
        hashCodeValid = true;
        return result;
    }

    /**
     * {@return the hash code value}
     */
    protected abstract int computeHashCode();

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final var equalityClass = equalityClass();
        return equalityClass.isInstance(obj) && computeEquals(equalityClass.cast(obj));
    }

    /**
     * {@return {@code true} if supplier {@code other} compares as equal to this object}.
     * @param other the other object
     */
    protected abstract boolean computeEquals(@NonNull T other);

    @Override
    public final String toString() {
        return computeToString();
    }

    /**
     * {@return the {@link #toString()} string}
     */
    protected abstract @NonNull String computeToString();
}
