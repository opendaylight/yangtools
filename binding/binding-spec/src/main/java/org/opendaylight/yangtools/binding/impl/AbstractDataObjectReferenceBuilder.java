/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectReference.Builder;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.InexactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.NodeStep;

/**
 * Base implementation of {@link Builder}.
 */
public abstract sealed class AbstractDataObjectReferenceBuilder<T extends DataObject> implements Builder<T>
        permits AbstractDataObjectIdentifierBuilder, DataObjectReferenceBuilder, DataObjectReferenceBuilderWithKey {
    private final ArrayList<@NonNull DataObjectStep<?>> pathBuilder;
    private final Iterable<? extends @NonNull DataObjectStep<?>> basePath;

    private boolean wildcard;

    protected AbstractDataObjectReferenceBuilder(final AbstractDataObjectReferenceBuilder<?> prev) {
        pathBuilder = prev.pathBuilder;
        basePath = prev.basePath;
        wildcard = prev.wildcard;
    }

    protected AbstractDataObjectReferenceBuilder(final DataObjectReference<T> base) {
        pathBuilder = new ArrayList<>(4);
        wildcard = base.isWildcarded();
        basePath = base.steps();
    }

    protected AbstractDataObjectReferenceBuilder(final DataObjectStep<?> item) {
        pathBuilder = new ArrayList<>(4);
        basePath = null;
        pathBuilder.add(requireNonNull(item));
        wildcard = item instanceof InexactDataObjectStep;
    }

    protected AbstractDataObjectReferenceBuilder(final ExactDataObjectStep<?> item) {
        pathBuilder = new ArrayList<>(4);
        basePath = null;
        pathBuilder.add(requireNonNull(item));
        wildcard = false;
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
    public <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>> WithKey<N, K> child(
            final Class<N> listItem, final K listKey) {
        return append(new KeyStep<>(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
            N extends EntryObject<N, K> & ChildOf<? super C>>
            WithKey<N, K> child(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    public abstract DataObjectReference<T> build();

    protected abstract <X extends DataObject> @NonNull Builder<X> append(@NonNull DataObjectStep<X> step);

    protected abstract <X extends EntryObject<X, Y>, Y extends Key<X>> @NonNull WithKey<X, Y> append(
        @NonNull KeyStep<Y, X> step);

    protected final boolean wildcard() {
        return wildcard;
    }

    protected final void appendItem(final DataObjectStep<?> item) {
        switch (item) {
            case ExactDataObjectStep<?> exact -> appendItem(exact);
            case InexactDataObjectStep<?> inexact -> appendItem(inexact);
        }
    }

    protected final void appendItem(final @NonNull ExactDataObjectStep<?> item) {
        pathBuilder.add(item);
    }

    // see AbstractDataObjectIdentifierBuilder.appendItem()
    protected void appendItem(final @NonNull InexactDataObjectStep<?> item) {
        pathBuilder.add(item);
        wildcard = true;
    }

    protected final @NonNull Iterable<? extends @NonNull DataObjectStep<?>> buildSteps() {
        final var prefix = basePath;
        if (prefix == null) {
            return pathBuilder.isEmpty() ? ImmutableList.of() : ImmutableList.copyOf(pathBuilder);
        }

        return switch (pathBuilder.size()) {
            case 0 -> prefix;
            case 1 -> new AppendIterable<>(prefix, pathBuilder.getFirst());
            default -> ImmutableList.<DataObjectStep<?>>builder().addAll(prefix).addAll(pathBuilder).build();
        };
    }
}
