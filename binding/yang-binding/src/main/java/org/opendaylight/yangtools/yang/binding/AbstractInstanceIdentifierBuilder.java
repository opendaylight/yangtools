/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

abstract sealed class AbstractInstanceIdentifierBuilder<T extends DataObject> implements InstanceIdentifierBuilder<T> {
    static final class Regular<T extends DataObject> extends AbstractInstanceIdentifierBuilder<T> {
        private @NonNull Class<T> type;

        Regular(final Item<T> item) {
            super(item, Identifiable.class.isAssignableFrom(item.getType()));
            type = item.getType();
        }

        Regular(final InstanceIdentifier<T> identifier) {
            super(identifier);
            type = identifier.getTargetType();
        }

        private Regular(final Keyed<?, ?> prev, final Item<T> item, final boolean wildcard) {
            super(prev, item, wildcard);
            type = item.getType();
        }

        @Override
        public InstanceIdentifier<T> build() {
            return new InstanceIdentifier<>(type, pathArguments(), wildcard(), hashCode());
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        <X extends DataObject> Regular<X> append(final Item<X> item, final boolean isWildcard) {
            appendItem(item, isWildcard);
            type = (Class) item.getType();
            return (Regular<X>) this;
        }

        @Override
        <X extends DataObject & Identifiable<Y>, Y extends Identifier<X>> Keyed<X, Y> append(
                final IdentifiableItem<X, Y> item) {
            return new Keyed<>(this, item);
        }
    }

    static final class Keyed<T extends DataObject & Identifiable<K>, K extends Identifier<T>>
            extends AbstractInstanceIdentifierBuilder<T> implements InstanceIdentifier.KeyedBuilder<T, K> {
        private @NonNull IdentifiableItem<T, K> lastItem;

        Keyed(final IdentifiableItem<T, K> item) {
            super(item, false);
            lastItem = requireNonNull(item);
        }

        Keyed(final KeyedInstanceIdentifier<T, K> identifier) {
            super(identifier);
            lastItem = IdentifiableItem.of(identifier.getTargetType(), identifier.getKey());
        }

        private Keyed(final Regular<?> prev, final IdentifiableItem<T, K> item) {
            super(prev, item, false);
            lastItem = requireNonNull(item);
        }

        @Override
        public KeyedInstanceIdentifier<T, K> build() {
            return new KeyedInstanceIdentifier<>(lastItem.getType(), pathArguments(), wildcard(), hashCode(),
                lastItem.getKey());
        }

        @Override
        <X extends DataObject> @NonNull Regular<X> append(final Item<X> item, final boolean isWildcard) {
            return new Regular<>(this, item, isWildcard);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        <X extends DataObject & Identifiable<Y>, Y extends Identifier<X>> Keyed<X, Y> append(
                final IdentifiableItem<X, Y> item) {
            appendItem(item, false);
            lastItem = (IdentifiableItem) item;
            return (Keyed<X, Y>) this;
        }
    }

    private final ImmutableList.Builder<PathArgument> pathBuilder;
    private final HashCodeBuilder<PathArgument> hashBuilder;
    private final Iterable<? extends PathArgument> basePath;

    private boolean wildcard;

    AbstractInstanceIdentifierBuilder(final AbstractInstanceIdentifierBuilder<?> prev, final PathArgument item,
            final boolean isWildcard) {
        pathBuilder = prev.pathBuilder;
        hashBuilder = prev.hashBuilder;
        basePath = prev.basePath;
        wildcard = prev.wildcard;
        appendItem(item, isWildcard);
    }

    AbstractInstanceIdentifierBuilder(final InstanceIdentifier<T> identifier) {
        pathBuilder = ImmutableList.builder();
        hashBuilder = new HashCodeBuilder<>(identifier.hashCode());
        wildcard = identifier.isWildcarded();
        basePath = identifier.pathArguments;
    }

    AbstractInstanceIdentifierBuilder(final PathArgument item, final boolean wildcard) {
        pathBuilder = ImmutableList.builder();
        hashBuilder = new HashCodeBuilder<>();
        basePath = null;
        hashBuilder.addArgument(item);
        pathBuilder.add(item);
        this.wildcard = wildcard;
    }

    final boolean wildcard() {
        return wildcard;
    }

    @Override
    public final <N extends DataObject & Augmentation<? super T>> Regular<N> augmentation(final Class<N> container) {
        return append(Item.of(container), false);
    }

    @Override
    public final <N extends ChildOf<? super T>> Regular<N> child(final Class<N> container) {
        return append(Item.of(container), Identifiable.class.isAssignableFrom(container));
    }

    @Override
    public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Regular<N> child(
            final Class<C> caze, final Class<N> container) {
        return append(Item.of(caze, container), Identifiable.class.isAssignableFrom(container));
    }

    @Override
    public final <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> Keyed<N, K> child(
            final Class<@NonNull N> listItem, final K listKey) {
        return append(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    public final <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> Keyed<N, K> child(final Class<C> caze,
                final Class<N> listItem, final K listKey) {
        return append(IdentifiableItem.of(caze, listItem, listKey));
    }

    final Iterable<PathArgument> pathArguments() {
        final var args = pathBuilder.build();
        return basePath == null ? args : Iterables.concat(basePath, args);
    }

    final void appendItem(final PathArgument item, final boolean isWildcard) {
        hashBuilder.addArgument(item);
        pathBuilder.add(item);
        wildcard |= isWildcard;
    }

    abstract <X extends DataObject> @NonNull Regular<X> append(Item<X> item, boolean isWildcard);

    abstract <X extends DataObject & Identifiable<Y>, Y extends Identifier<X>>
        @NonNull Keyed<X, Y> append(IdentifiableItem<X, Y> item);

    @Override
    public final int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractInstanceIdentifierBuilder<?> other
            && wildcard == other.wildcard && hashCode() == other.hashCode()
            && Iterables.elementsEqual(pathArguments(), other.pathArguments());
    }
}
