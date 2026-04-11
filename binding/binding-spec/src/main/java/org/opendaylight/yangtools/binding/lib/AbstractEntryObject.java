/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;

/**
 * Abstract base class for implementations of {@link EntryObject}.
 *
 * @param <T> {@link EntryObject} type
 * @param <K> {@link Key} type
 */
public abstract class AbstractEntryObject<T extends EntryObject<T, K>, K extends Key<T>>
        extends AbstractAugmentable<T> implements EntryObject<T, K> {
    private final @NonNull K key;

    protected AbstractEntryObject(final Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations,
            final K key) {
        super(augmentations);
        this.key = requireNonNull(key);
    }

    @Override
    public final K key() {
        return key;
    }
}
