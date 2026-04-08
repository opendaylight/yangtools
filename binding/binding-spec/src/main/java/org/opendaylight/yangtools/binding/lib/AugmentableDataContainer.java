/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * An extension of {@link AbstractAugmentable} for {@link DataContainer} specializations. It implements
 * {@link #hashCode()} caching and common handling of {@link #equals(Object)}.
 *
 * @param <T> the {@link Augmentable} {@link DataContainer} type
 * @since 15.0.3
 */
public abstract class AugmentableDataContainer<T extends DataContainer & Augmentable<T>> extends AbstractAugmentable<T> {
    private static final VarHandle VH;

    static {
        try {
            VH = MethodHandles.lookup().findVarHandle(AugmentableDataContainer.class, "hashBits", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private int hashBits;

    /**
     * {@return the equality class, should always be the same as {@link BindingContract#implementedInterface()}}
     */
    protected abstract @NonNull Class<T> equalityClass();

    protected AugmentableDataContainer(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations) {
        super(augmentations);
    }

    @Override
    public final int hashCode() {
        final var bits = (int) VH.getAcquire(this);
        return bits != 0 ? bits : loadHashCode();
    }

    private int loadHashCode() {
        final var result = computeHashCode() | 1;
        VH.setRelease(this, result);
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
}
