/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * An extension of {@link AbstractAugmentable} for {@link DataContainer} specializations. It implements
 * {@link #hashCode()} caching and common handling of {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <T> the {@link Augmentable} {@link DataContainer} type
 * @since 15.1.0
 */
// FIXME: remove when AbstractAugmentable extends AbstractDataContainer
@Beta
public abstract class AugmentableDataContainer<T extends DataContainer & Augmentable<T>> extends AbstractAugmentable<T>
        implements JavaDataContainer<T> {
    // TODO: single field when hashCode() is defined to be != 0
    private int hashCode;
    private volatile boolean hashCodeValid;

    protected AugmentableDataContainer(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations) {
        super(augmentations);
    }

    @Override
    public final int hashCode() {
        return hashCodeValid ? hashCode : loadHashCode();
    }

    private int loadHashCode() {
        final var result = bindingHashCode();
        hashCode = result;
        hashCodeValid = true;
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || implementedInterface().isInstance(obj) && bindingEquals((T) obj);
    }

    @Override
    public final String toString() {
        return bindingToString();
    }
}
