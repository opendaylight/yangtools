/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyStep;

sealed class DataObjectReferenceBuilder<T extends DataObject> extends AbstractDataObjectReferenceBuilder<T>
        permits DataObjectReferenceBuilderWithKey {
    DataObjectReferenceBuilder(final AbstractDataObjectReferenceBuilder<?> prev, final DataObjectStep<?> item) {
        super(prev, item);
    }

    DataObjectReferenceBuilder(final DataObjectReference<T> base) {
        super(base);
    }

    DataObjectReferenceBuilder(final DataObjectStep<?> item, final boolean wildcard) {
        super(item, wildcard);
    }

    @Override
    public DataObjectReferenceImpl<T> build() {
        final var steps = buildSteps();
        return wildcard() ? new DataObjectReferenceImpl<>(steps) : new DataObjectReferenceImpl<>(steps);
    }

    @Override
    public <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> WithKey<N, K> child(final Class<N> listItem,
            final K listKey) {
        return append(new KeyStep<>(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>, N extends KeyAware<K> & ChildOf<? super C>>
            WithKey<N, K> child(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    protected <X extends DataObject> AbstractDataObjectReferenceBuilder<X> append(final DataObjectStep<X> step) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <X extends DataObject & KeyAware<Y>, Y extends Key<X>> DataObjectReferenceBuilderWithKey<X, Y> append(
            final KeyStep<Y, X> step) {
        // TODO Auto-generated method stub
        return null;
    }
}
