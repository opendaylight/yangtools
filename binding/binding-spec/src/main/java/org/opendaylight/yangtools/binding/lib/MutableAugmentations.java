/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A mutable {@link Augmentations}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.0.1
 */
@Beta
public sealed interface MutableAugmentations<C extends DataContainer & Augmentable<C>>
        extends Augmentations<C>, Mutable permits HashAugmentations {
    /**
     * Add an {@link Augmentation}, potentially replacing a previous augmentation of the same type.
     *
     * @param value the {@link Augmentation}
     * @throws ClassCastException if value is not consistent with its implemented interface
     * @throws NullPointerException if value is {@code null}
     * @see ImmutableAugmentations#with(Augmentation)
     */
    default void set(final Augmentation<C, ?> value) {
        // implemented interface must be a subclass of Augmentation
        @SuppressWarnings("unchecked")
        final var implementedInterface = (Class<? extends Augmentation<C, ?>>) value.implementedInterface()
            .asSubclass(Augmentation.class);
        // implemented interface must actually be implemented
        put(implementedInterface, implementedInterface.cast(value));
    }

    /**
     * Remove the {@link Augmentation} of specified type.
     *
     * @param key the {@link Augmentation} type
     * @throws ClassCastException if type is not a subclass of {@link Augmentation}
     * @throws NullPointerException if type is {@code null}
     * @see ImmutableAugmentations#without(Class)
     */
    default void unset(final Class<? extends Augmentation<C, ?>> key) {
        remove(key.asSubclass(Augmentation.class));
    }
}
