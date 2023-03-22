/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

final class InstanceIdentifierBuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {
    private final ImmutableList.Builder<PathArgument> pathBuilder = ImmutableList.builder();
    private final HashCodeBuilder<PathArgument> hashBuilder;
    private final Iterable<? extends PathArgument> basePath;
    private boolean wildcard;
    private PathArgument arg;

    private InstanceIdentifierBuilderImpl(final PathArgument item) {
        hashBuilder = new HashCodeBuilder<>();
        basePath = null;
        arg = requireNonNull(item);
        hashBuilder.addArgument(item);
        pathBuilder.add(item);
        wildcard = false;
    }

    InstanceIdentifierBuilderImpl(final IdentifiableItem<? super T, ?> item) {
        this((PathArgument) item);
    }

    InstanceIdentifierBuilderImpl(final Item<T> item) {
        this((PathArgument) item);
        if (Identifiable.class.isAssignableFrom(item.getType())) {
            wildcard = true;
        }
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard) {
        hashBuilder = new HashCodeBuilder<>(hash);
        basePath = pathArguments;
        this.wildcard = wildcard;
        arg = item;
    }

    @Override
    public int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof InstanceIdentifierBuilderImpl<?> other
            && wildcard == other.wildcard && Objects.equals(basePath, other.basePath) && Objects.equals(arg, other.arg)
            && hashBuilder.build() == other.hashBuilder.build();
    }

    @Override
    public <N extends ChildOf<? super T>> InstanceIdentifierBuilderImpl<N> child(final Class<N> container) {
        return addNode(container);
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> InstanceIdentifierBuilder<N>
            child(final Class<C> caze, final Class<N> container) {
        return addWildNode(Item.of(caze, container));
    }

    @Override
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilderImpl<N>
            child(final Class<@NonNull N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
        N extends Identifiable<K> & ChildOf<? super C>> InstanceIdentifierBuilder<N> child(final Class<C> caze,
                final Class<N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(caze, listItem, listKey));
    }

    /**
     * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
     * the builder.
     *
     * @param container Augmentation to be added
     * @param <N> Augmentation type
     * @return This builder
     */
    @Override
    public <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilderImpl<N> augmentation(
            final Class<N> container) {
        return addNode(container);
    }

    @Override
    public InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        return InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
    }

    private <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addWildNode(final PathArgument newArg) {
        if (Identifiable.class.isAssignableFrom(newArg.getType())) {
            wildcard = true;
        }
        return addNode(newArg);
    }

    @SuppressWarnings("unchecked")
    private <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    private <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Class<N> container) {
        return addWildNode(Item.of(container));
    }
}
