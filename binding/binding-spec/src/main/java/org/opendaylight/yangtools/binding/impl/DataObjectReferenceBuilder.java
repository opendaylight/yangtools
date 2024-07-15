/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectReferenceBuilder<T extends DataObject> extends AbstractDataObjectReferenceBuilder<T> {
    DataObjectReferenceBuilder(final AbstractDataObjectReferenceBuilder<?> prev) {
        super(prev);
    }

    DataObjectReferenceBuilder(final DataObjectReference<T> base) {
        super(base);
    }

    public DataObjectReferenceBuilder(final DataObjectStep<?> item) {
        super(item);
    }

    @Override
    public DataObjectReference<T> build() {
        final var steps = buildSteps();
        return wildcard() ? new DataObjectReferenceImpl<>(steps) : new DataObjectIdentifierImpl<>(null, steps);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <X extends DataObject> DataObjectReferenceBuilder<X> append(final DataObjectStep<X> step) {
        appendItem(step);
        return (DataObjectReferenceBuilder<X>) this;
    }

    @Override
    protected <X extends EntryObject<X, Y>, Y extends Key<X>> DataObjectReferenceBuilderWithKey<X, Y> append(
            final KeyStep<Y, X> step) {
        return new DataObjectReferenceBuilderWithKey<X, Y>(this).append(step);
    }
}
