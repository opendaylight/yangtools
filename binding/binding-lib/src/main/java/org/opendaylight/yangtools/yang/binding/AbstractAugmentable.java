/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract base class for implementing immutable {@link Augmentable} classes. This class is provided as a convenience.
 *
 * @param <T> Augmentable type
 */
@Beta
public abstract class AbstractAugmentable<T extends Augmentable<T>> implements Augmentable<T> {
    private final @NonNull ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations;

    protected AbstractAugmentable() {
        this.augmentations = ImmutableMap.of();
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
}
