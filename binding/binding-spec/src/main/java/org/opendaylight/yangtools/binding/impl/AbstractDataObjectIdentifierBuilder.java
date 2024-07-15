/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.Builder;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.InexactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.NodeStep;

public abstract sealed class AbstractDataObjectIdentifierBuilder<T extends DataObject>
        extends AbstractDataObjectReferenceBuilder<T>
        implements DataObjectIdentifier.Builder<T>
        permits DataObjectIdentifierBuilder, DataObjectIdentifierBuilderWithKey {
    AbstractDataObjectIdentifierBuilder(final AbstractDataObjectIdentifierBuilder<?> prev) {
        super(prev);
    }

    AbstractDataObjectIdentifierBuilder(final DataObjectIdentifier<T> base) {
        super(base);
    }

    AbstractDataObjectIdentifierBuilder(final ExactDataObjectStep<?> item) {
        super(item);
    }

    @Override
    public <A extends Augmentation<? super T>> Builder<A> augmentation(final Class<A> augmentation) {
        return append(new NodeStep<>(augmentation));
    }

    @Override
    public <N extends ChildOf<? super T>> Builder<N> child(final Class<N> container) {
        return append(DataObjectStep.of(container));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
            final Class<C> caze, final Class<N> container) {
        return append(DataObjectStep.of(caze, container));
    }

    @Override
    public <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>> Builder.WithKey<N, K> child(
            final Class<N> listItem, final K listKey) {
        return append(new KeyStep<>(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
            N extends EntryObject<N, K> & ChildOf<? super C>>
            Builder.WithKey<N, K> child(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    protected final void appendItem(final InexactDataObjectStep<?> item) {
        throw new IllegalArgumentException("Cannot make inexact step " + item);
    }

    @Override
    protected abstract <X extends DataObject> Builder<X> append(DataObjectStep<X> step);

    @Override
    protected abstract <X extends EntryObject<X, Y>, Y extends Key<X>> Builder.WithKey<X, Y> append(KeyStep<Y, X> step);
}
