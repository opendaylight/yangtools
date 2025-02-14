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
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectIdentifierBuilder<T extends DataObject> extends AbstractDataObjectIdentifierBuilder<T> {
    DataObjectIdentifierBuilder(final AbstractDataObjectIdentifierBuilder<?> prev) {
        super(prev);
    }

    DataObjectIdentifierBuilder(final DataObjectIdentifier<T> base) {
        super(base);
    }

    DataObjectIdentifierBuilder(final ExactDataObjectStep<?> item) {
        super(item);
    }

    public DataObjectIdentifierBuilder(final DataObjectStep<?> item) {
        this(validate(item));
    }

    @Override
    public DataObjectIdentifier<T> build() {
        return new DataObjectIdentifierImpl<>(null, buildSteps());
    }

    @Override
    public DataObjectReference.Builder<T> toReferenceBuilder() {
        return new DataObjectReferenceBuilder<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <X extends DataObject> DataObjectIdentifierBuilder<X> append(final DataObjectStep<X> step) {
        appendItem(step);
        return (DataObjectIdentifierBuilder<X>) this;
    }

    @Override
    protected <X extends EntryObject<X, Y>, Y extends Key<X>> DataObjectIdentifierBuilderWithKey<X, Y> append(
            final KeyStep<Y, X> step) {
        return new DataObjectIdentifierBuilderWithKey<X, Y>(this).append(step);
    }

    private static <T extends DataObject> ExactDataObjectStep<T> validate(final DataObjectStep<T> step) {
        if (step instanceof ExactDataObjectStep<T> exact) {
            return exact;
        }
        throw new IllegalArgumentException("Cannot use with " + step);
    }
}
