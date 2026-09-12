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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
    static <C extends Augmentable<C> & DataContainer> @NonNull Augmentations<C> copyOf(
            final Map<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> map) {
        return switch (map) {
            case ImmutableAugmentations<C> immutable -> immutable;
            case HashAugmentations<C> hash -> hash.clone();
            default -> switch (map.size()) {
                case 0 -> ImmutableAugmentations.of();
                case 1 -> ImmutableAugmentations.of(map.values().iterator().next());
                default ->  new HashAugmentations<>(map);
            };
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
    default <A extends Augmentation<C, A>> @Nullable A lookup(final Class<A> key) {
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
}
