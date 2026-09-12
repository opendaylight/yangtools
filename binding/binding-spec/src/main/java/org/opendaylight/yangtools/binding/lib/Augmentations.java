/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.util.Map;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * A set of {@link Augmentation}s attached to an {@link Augmentable}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.0.1
 */
@Beta
public sealed interface Augmentations<C extends Augmentable<C> & DataContainer>
        extends Map<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>>
        permits ImmutableAugmentations, MutableAugmentations {
    /**
     * Return an {@link Augmentations} instance containing same mappings as the specified map.
     *
     * @param <C> the {@link Augmentable} {@link DataContainer} type
     * @param map the map
     * @return an {@link Augmentations} instance
     * @throws NullPointerException if augmentation is {@code null}
     */
    static <C extends Augmentable<C> & DataContainer> Augmentations<C> copyOf(
            final Map<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> map) {
        return switch (map) {
            case HashAugmentations<C> hash -> hash.clone();
            case ImmutableAugmentations<C> immutable -> immutable;
            default -> map.isEmpty() ? ImmutableAugmentations.of() : new HashAugmentations<>(map);
        };
    }

    /**
     * {@return an {@link Augmentation} of specified type, or {@code null} if not present}
     *
     * @param <A> the {@link Augmentation} type
     * @param key the type class
     * @throws NullPointerException if type is {@code null}
     * @throws ClassCastException if type is not a subclass of {@link Augmentation}
     */
    default <A extends Augmentation<C, A>> A lookup(final Class<A> key) {
        final var augmentation = get(key.asSubclass(Augmentation.class));
        if (augmentation == null) {
            return null;
        }
        try {
            return key.cast(augmentation);
        } catch (ClassCastException e) {
            throw new VerifyException("Inconsistent index", e);
        }
    }

    /**
     * Add an {@link Augmentation}, potentially replacing a previous augmentation of the same type.
     *
     * @param value the {@link Augmentation}
     * @throws ClassCastException if augmentation is not consistent with its implemented interface
     * @throws NullPointerException if augmentation is {@code null}
     * @throws UnsupportedOperationException if addition is not supported
     */
    default void set(final Augmentation<C, ?> value) {
        // implemented interface must be a subclass of Augmentation
        @SuppressWarnings("unchecked")
        final var implementedInterface = (Class<? extends Augmentation<C, ?>>)
            value.implementedInterface().asSubclass(Augmentation.class);
        // implemented interface must actually be implemented
        put(implementedInterface, implementedInterface.cast(value));
    }
}
