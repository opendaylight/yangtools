/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyStep;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public final class KeyedInstanceIdentifier<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends InstanceIdentifier<T> implements WithKey<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull KeyStep<K, T> lastStep;

    KeyedInstanceIdentifier(final KeyStep<K, T> lastStep, final Iterable<? extends DataObjectStep<?>> pathArguments,
            final boolean wildcarded) {
        super(lastStep.type(), pathArguments, wildcarded);
        this.lastStep = lastStep;
    }

    @NonNull KeyStep<K, T> lastStep() {
        return lastStep;
    }

    @Override
    public K key() {
        return lastStep.key();
    }

    @Override
    public KeyedBuilder<T, K> toBuilder() {
        return new KeyedBuilder<>(this);
    }
}
