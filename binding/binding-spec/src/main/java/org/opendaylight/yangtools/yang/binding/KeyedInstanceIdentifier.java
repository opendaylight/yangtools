/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierWithKey;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceWithKey;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
@Deprecated(since = "14.0.23", forRemoval = true)
public final class KeyedInstanceIdentifier<T extends EntryObject<T, K>, K extends Key<T>>
        extends InstanceIdentifier<T> implements WithKey<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    KeyedInstanceIdentifier(final Iterable<? extends DataObjectStep<?>> pathArguments, final boolean wildcarded) {
        super(pathArguments, wildcarded);
    }

    @Override
    public KeyStep<K, T> lastStep() {
        return getLast(steps());
    }

    @Override
    @Deprecated(since = "14.0.0")
    public KeyedBuilder<T, K> builder() {
        return toBuilder();
    }

    @Override
    public KeyedInstanceIdentifier<T, K> toLegacy() {
        return this;
    }

    @Override
    public KeyedBuilder<T, K> toBuilder() {
        return new KeyedBuilder<>(this);
    }

    @Override
    public DataObjectIdentifier.WithKey<T, K> toIdentifier() {
        return toReference().toIdentifier();
    }

    @Override
    public DataObjectReference.WithKey<T, K> toReference() {
        final var steps = steps();
        return isExact() ? new DataObjectIdentifierWithKey<>(null, steps) : new DataObjectReferenceWithKey<>(steps);
    }
}
