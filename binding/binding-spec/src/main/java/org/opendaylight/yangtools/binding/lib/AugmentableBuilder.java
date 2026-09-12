/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Abstract base class for Builders that produce {@link Augmentable} {@link DataContainer}s. Instances of this class
 * along with all its subclasses are mutable and are not expected to be used from multiple threads concurrently.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @param <T> builder type
 * @since 16.0.1
 */
public abstract class AugmentableBuilder<C extends DataContainer & Augmentable<C>, T extends AugmentableBuilder<C, T>>
        implements Mutable {
    private @NonNull Augmentations<C> augmentations;

    /**
     * Constract a builder initialized with no augmentations.
     */
    protected AugmentableBuilder() {
        augmentations = ImmutableAugmentations.of();
    }

    /**
     * Construct a builder initialized with augmentations present in the specified container.
     *
     * @param container the container
     * @throws NullPointerException if {@code container} is {@code null}
     */
    protected AugmentableBuilder(final @NonNull C container) {
        augmentations = Augmentations.copyOf(container.augmentations());
    }

    /**
     * Return the specified augmentation, if it is present in this builder.
     *
     * @param <A> augmentation type
     * @param augmentationType augmentation type class
     * @return Augmentation object from this builder, or {@code null} if not present
     * @throws NullPointerException if {@code augmentType} is {@code null}
     * @deprecated This method will not be generated in a future release
     */
    @Deprecated(forRemoval = true)
    public final <A extends Augmentation<C, A>> @Nullable A augmentation(final Class<A> augmentationType) {
        return augmentations.lookup(augmentationType);
    }

    /**
     * Add an augmentation to this builder's product.
     *
     * @param augmentation augmentation to be added
     * @return this builder
     * @throws NullPointerException if {@code augmentation} is {@code null}
     */
    public final @NonNull T addAugmentation(final Augmentation<C, ?> augmentation) {
        switch (augmentations) {
            case ImmutableAugmentations<C> immutable -> augmentations = immutable.with(augmentation);
            case MutableAugmentations<C> mutable -> mutable.set(augmentation);
        }
        return self();
    }

    /**
     * Remove an augmentation from this builder's product. If this builder does not track such an augmentation
     * type, this method does nothing.
     *
     * @param type augmentation type to be removed
     * @return this builder
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public final @NonNull T removeAugmentation(final Class<? extends Augmentation<C, ?>> type) {
        switch (augmentations) {
            case ImmutableAugmentations<C> immutable -> augmentations = immutable.without(type);
            case MutableAugmentations<C> mutable -> mutable.unset(type);
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    private @NonNull T self() {
        return (@NonNull T) this;
    }

    /**
     * {@return currently set {@link Augmentations}}
     */
    protected final @NonNull Augmentations<C> augmentations() {
        return augmentations;
    }
}
