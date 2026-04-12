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
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;

/**
 * An extension of {@link AbstractEntryObject} implementing {@link #hashCode()} caching and common handling of
 * {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <T> {@link EntryObject} type
 * @param <K> {@link Key} type
 * @since 15.1.0
 */
// FIXME: remove when AbstractAugmentable extends AbstractDataContainer
@Beta
public abstract class BaseEntryObject<T extends EntryObject<T, K>, K extends Key<T>> extends AbstractEntryObject<T, K>
        implements JavaContract<DataContainer, T> {
    // TODO: single field when hashCode() is defined to be != 0
    private int hashCode;
    private volatile boolean hashCodeValid;

    protected BaseEntryObject(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations, final K key) {
        super(augmentations, key);
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
