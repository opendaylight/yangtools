/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import org.opendaylight.yangtools.binding.DataContainer.Addressable;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectReference.Builder.WithKey;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectReferenceBuilderWithKey<T extends EntryObject<T, K>, K extends Key<T>>
        extends AbstractDataObjectReferenceBuilder<T> implements WithKey<T, K> {
    DataObjectReferenceBuilderWithKey(final DataObjectReferenceBuilder<?> prev, final DataObjectStep<?> item) {
        super(prev, item);
    }

    DataObjectReferenceBuilderWithKey(final DataObjectReference.WithKey<T, K> base) {
        super(base);
    }

    DataObjectReferenceBuilderWithKey(final KeyStep<K, T> item, final boolean wildcard) {
        super(item, wildcard);
    }

    @Override
    public DataObjectReference.WithKey<T, K> build() {
        final var steps = buildSteps();
        return wildcard() ? new DataObjectReferenceWithKey<>(steps) : new DataObjectIdentifierWithKey<>(null, steps);
    }

    @Override
    protected <X extends Addressable> DataObjectReferenceBuilder<X> append(final DataObjectStep<X> step) {
        return new DataObjectReferenceBuilder<>(this, step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <X extends EntryObject<X, Y>, Y extends Key<X>> DataObjectReferenceBuilderWithKey<X, Y> append(
            final KeyStep<Y, X> step) {
        appendItem(step);
        return (DataObjectReferenceBuilderWithKey<X, Y>) this;
    }
}
