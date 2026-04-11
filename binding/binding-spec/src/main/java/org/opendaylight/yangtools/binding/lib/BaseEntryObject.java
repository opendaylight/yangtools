/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;

/**
 * An extension of {@link AbstractEntryObject} implementing {@link #hashCode()} caching and common handling of
 * {@link #equals(Object)}.
 *
 * @param <T> {@link EntryObject} type
 * @param <K> {@link Key} type
 * @since 15.1.0
 */
public abstract class BaseEntryObject<T extends EntryObject<T, K>, K extends Key<T>> extends AbstractEntryObject<T, K> {
    // TODO: single field when hashCode() is defined to be != 0
    private int hashCode;
    private volatile boolean hashCodeValid;

    protected BaseEntryObject(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations, final K key) {
        super(augmentations, key);
    }

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
