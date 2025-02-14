/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.Builder.WithKey;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectIdentifierBuilderWithKey<T extends EntryObject<T, K>, K extends Key<T>>
        extends AbstractDataObjectIdentifierBuilder<T> implements WithKey<T, K> {
    DataObjectIdentifierBuilderWithKey(final DataObjectIdentifierBuilder<?> prev) {
        super(prev);
    }

    DataObjectIdentifierBuilderWithKey(final DataObjectIdentifier.WithKey<T, K> base) {
        super(base);
    }

    public DataObjectIdentifierBuilderWithKey(final KeyStep<K, T> item) {
        super(item);
    }

    @Override
    public DataObjectIdentifier.WithKey<T, K> build() {
        return new DataObjectIdentifierWithKey<>(null, buildSteps());
    }

    @Override
    public DataObjectReference.Builder.WithKey<T, K> toReferenceBuilder() {
        return new DataObjectReferenceBuilderWithKey<>(this);
    }

    @Override
    protected <X extends DataObject> DataObjectIdentifierBuilder<X> append(final DataObjectStep<X> step) {
        return new DataObjectIdentifierBuilder<X>(this).append(step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <X extends EntryObject<X, Y>, Y extends Key<X>> DataObjectIdentifierBuilderWithKey<X, Y> append(
            final KeyStep<Y, X> step) {
        appendItem(step);
        return (DataObjectIdentifierBuilderWithKey<X, Y>) this;
    }
}
