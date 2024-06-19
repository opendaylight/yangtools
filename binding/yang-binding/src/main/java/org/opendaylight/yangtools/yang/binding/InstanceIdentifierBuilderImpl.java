/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Objects;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

final class InstanceIdentifierBuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {
    private final ImmutableList.Builder<PathArgument> pathBuilder = ImmutableList.builder();
    private final HashCodeBuilder<PathArgument> hashBuilder;
    private final Iterable<? extends PathArgument> basePath;
    private boolean wildcard = false;
    private PathArgument arg = null;

    InstanceIdentifierBuilderImpl() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.basePath = null;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.wildcard = wildcard;
        this.arg = item;
    }

    @Override
    public int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof InstanceIdentifierBuilderImpl) {
            InstanceIdentifierBuilderImpl<T> otherBuilder = (InstanceIdentifierBuilderImpl<T>) obj;
            return wildcard == otherBuilder.wildcard && Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> InstanceIdentifierBuilderImpl<N> addNode(final Class<N> container) {
        arg = Item.of(container);
        hashBuilder.addArgument(arg);
        pathBuilder.add(arg);

        if (Identifiable.class.isAssignableFrom(container)) {
            wildcard = true;
        }

        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> InstanceIdentifierBuilderImpl<N> addNode(
            final Class<N> listItem, final K listKey) {
        arg = IdentifiableItem.of(listItem, listKey);
        hashBuilder.addArgument(arg);
        pathBuilder.add(arg);
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @Override
    public <N extends ChildOf<? super T>> InstanceIdentifierBuilderImpl<N> child(final Class<N> container) {
        return addNode(container);
    }

    @Override
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilderImpl<N>
            child(final Class<N> listItem, final K listKey) {
        return addNode(listItem, listKey);
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

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<T> ret = (InstanceIdentifier<T>) InstanceIdentifier.trustedCreate(arg, pathArguments,
            hashBuilder.build(), wildcard);
        return ret;
    }
}
