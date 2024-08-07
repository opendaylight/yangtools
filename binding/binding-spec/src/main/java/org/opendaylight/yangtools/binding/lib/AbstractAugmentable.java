/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;

/**
 * Abstract base class for implementing immutable {@link Augmentable} classes. This class is provided as a convenience.
 *
 * @param <T> Augmentable type
 */
public abstract class AbstractAugmentable<T extends Augmentable<T>> implements Augmentable<T> {
    private final @NonNull ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations;

    protected AbstractAugmentable() {
        augmentations = ImmutableMap.of();
    }

    protected AbstractAugmentable(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations) {
        this.augmentations = ImmutableMap.copyOf(augmentations);
    }

    protected AbstractAugmentable(
            final ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations) {
        this.augmentations = requireNonNull(augmentations);
    }

    protected AbstractAugmentable(final AbstractAugmentable<T> other) {
        this(other.augmentations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <A extends Augmentation<T>> A augmentation(final Class<A> augmentationType) {
        return (A) augmentations.get(requireNonNull(augmentationType));
    }

    @Override
    public final ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations() {
        return augmentations;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
