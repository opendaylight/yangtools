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
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Abstract base class for implementing immutable {@link Augmentable} classes. This class is provided as a convenience.
 *
 * @param <T> Augmentable type
 */
public abstract class AbstractAugmentable<T extends Augmentable<T> & DataContainer & JavaDataContainer<T>>
        extends AbstractDataContainer<T>
        implements Augmentable<T> {
    private final @NonNull Map<Class<? extends Augmentation<T, ?>>, Augmentation<T, ?>> augmentations;

    /**
     * Default constructor for empty {@link #augmentations()}.
     */
    protected AbstractAugmentable() {
        augmentations = ImmutableMap.of();
    }

    /**
     * Copy constructor from another {@link AbstractAugmentable}.
     *
     * @param otherthe other {@link AbstractAugmentable}
     */
    protected AbstractAugmentable(final AbstractAugmentable<T> other) {
        augmentations = other.augmentations;
    }

    /**
     * Constructor initializing {@link #augmentations} to specified {@link Augmentations}. The augmentations are
     * defensively copied if needed.
     *
     * @param augmentations the {@link Augmentations}
     */
    protected AbstractAugmentable(final Augmentations<T> augmentations) {
        this.augmentations = switch (augmentations) {
            case ImmutableAugmentations<T> immutable -> immutable;
            case MutableAugmentations<T> mutable -> ImmutableMap.copyOf(mutable);
        };
    }

    /**
     * Constructor initializing {@link #augmentations} to specified {@link MutableAugmentations}. The augmentations are
     * defensively copied.
     *
     * @param augmentations the {@link MutableAugmentations}
     */
    protected AbstractAugmentable(final MutableAugmentations<T> augmentations) {
        this(ImmutableMap.copyOf(augmentations));
    }

    /**
     * Constructor initializing {@link #augmentations} to specified {@link ImmutableAugmentations}.
     *
     * @param augmentations the {@link ImmutableAugmentations}
     */
    protected AbstractAugmentable(final ImmutableAugmentations<T> augmentations) {
        this.augmentations = requireNonNull(augmentations);
    }

    /**
     * Constructor initializing {@link #augmentations} to specified {@link Map}. The augmentations are defensively
     * copied if needed.
     *
     * @param augmentations the {@link Map}
     */
    protected AbstractAugmentable(final Map<Class<? extends Augmentation<T, ?>>, Augmentation<T, ?>> augmentations) {
        this.augmentations = switch (augmentations) {
            case ImmutableAugmentations<T> immutable -> immutable;
            default -> ImmutableMap.copyOf(augmentations);
        };
    }

    /**
     * Constructor initializing {@link #augmentations} to specified {@link ImmutableMap}.
     *
     * @param augmentations the {@link ImmutableMap}
     */
    protected AbstractAugmentable(
            final ImmutableMap<Class<? extends Augmentation<T, ?>>, Augmentation<T, ?>> augmentations) {
        this.augmentations = requireNonNull(augmentations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <A extends Augmentation<T, A>> A augmentation(final Class<A> augmentationType) {
        return (A) augmentations.get(requireNonNull(augmentationType));
    }

    @Override
    public final Map<Class<? extends Augmentation<T, ?>>, Augmentation<T, ?>> augmentations() {
        return augmentations;
    }
}
