/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.LeafsetEntryInterner;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class InterningLeafSetNodeBuilder<T> implements SystemLeafSetNode.Builder<T> {
    private final SystemLeafSetNode.Builder<T> delegate;
    private final LeafsetEntryInterner interner;

    private InterningLeafSetNodeBuilder(final SystemLeafSetNode.Builder<T> delegate,
            final LeafsetEntryInterner interner) {
        this.delegate = requireNonNull(delegate);
        this.interner = requireNonNull(interner);
    }

    private static @Nullable LeafsetEntryInterner getInterner(final @Nullable DataSchemaNode schema) {
        return schema instanceof LeafListSchemaNode leafListSchema ? LeafsetEntryInterner.forSchema(leafListSchema)
                : null;
    }

    static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> create(final @Nullable DataSchemaNode schema) {
        final var delegate = Builders.<T>leafSetBuilder();
        final var interner = getInterner(schema);
        return interner == null ? delegate : new InterningLeafSetNodeBuilder<>(delegate, interner);
    }

    static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> create(final @Nullable DataSchemaNode schema,
            final int sizeHint) {
        final var delegate = Builders.<T>leafSetBuilder(sizeHint);
        final var interner = getInterner(schema);
        return interner == null ? delegate : new InterningLeafSetNodeBuilder<>(delegate, interner);
    }

    @Override
    public ListNodeBuilder<T, SystemLeafSetNode<T>> withNodeIdentifier(final NodeIdentifier nodeIdentifier) {
        return delegate.withNodeIdentifier(nodeIdentifier);
    }

    @Override
    public ListNodeBuilder<T, SystemLeafSetNode<T>> withValue(final Collection<LeafSetEntryNode<T>> value) {
        // FIXME: pass through interner
        return delegate.withValue(value);
    }

    @Override
    public ListNodeBuilder<T, SystemLeafSetNode<T>> withChild(final LeafSetEntryNode<T> child) {
        return delegate.withChild(interner.intern(child));
    }

    @Override
    public ListNodeBuilder<T, SystemLeafSetNode<T>> withoutChild(final PathArgument key) {
        return delegate.withoutChild(key);
    }

    @Override
    public ListNodeBuilder<T, SystemLeafSetNode<T>> withChildValue(final T child) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, LeafSetEntryNode<T>, SystemLeafSetNode<T>>
            addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, LeafSetEntryNode<T>, SystemLeafSetNode<T>>
            removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    @Override
    public SystemLeafSetNode<T> build() {
        return delegate.build();
    }
}
