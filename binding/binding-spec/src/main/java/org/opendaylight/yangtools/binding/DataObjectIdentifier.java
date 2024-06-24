/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.KeyedBuilder;

/**
 * A {@link DataObjectReference} matching at most one {@link DataObject}, consistent with YANG
 * {@code instance-identifier} addressing as captured by {@link BindingInstanceIdentifier}.
 *
 * @param <T> type of {@link DataObject} held in the last step.
 */
// FIXME: YANGTOOLS-1577: seal this type
public non-sealed interface DataObjectIdentifier<T extends DataObject>
        extends DataObjectReference<T>, BindingInstanceIdentifier {
    /**
     * A {@link DataObjectIdentifier} pointing to a {@link KeyAware} {@link DataObject}, typically a map entry.
     *
     * @param <K> Key type
     * @param <T> KeyAware type
     */
    non-sealed interface WithKey<T extends KeyAware<K> & DataObject, K extends Key<T>>
            extends DataObjectIdentifier<T>, DataObjectReference.WithKey<T, K> {
        @Override
        KeyedBuilder<T, K> toBuilder();
    }

    @Override
    Iterable<? extends @NonNull ExactDataObjectStep<?>> steps();

    @Override
    default boolean isExact() {
        return true;
    }

    @Override
    @Deprecated(since = "14.0.0")
    default boolean isWildcarded() {
        return false;
    }
}
