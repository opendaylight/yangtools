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
