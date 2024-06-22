/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public final class KeyedInstanceIdentifier<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends InstanceIdentifier<T> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    KeyedInstanceIdentifier(final Iterable<DataObjectStep<?>> steps, final KeyStep<K, T> lastStep) {
        super(steps, lastStep);
    }

    @Override
    @SuppressWarnings("unchecked")
    public KeyStep<K, T> lastStep() {
        return (KeyStep<K, T>) super.lastStep();
    }

    /**
     * Return the key attached to this identifier. This method is equivalent to calling
     * {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
     *
     * @return Key associated with this instance identifier.
     */
    public @NonNull K getKey() {
        return lastStep().key();
    }

    @Override
    public KeyedBuilder<T, K> toBuilder() {
        return new KeyedBuilder<>(this);
    }

    @Override
    boolean keyEquals(final InstanceIdentifier<?> other) {
        return getKey().equals(((KeyedInstanceIdentifier<?, ?>) other).getKey());
    }
}
