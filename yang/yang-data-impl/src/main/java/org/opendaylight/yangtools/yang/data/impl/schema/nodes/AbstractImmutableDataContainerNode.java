/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Collections;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public abstract class AbstractImmutableDataContainerNode<K extends PathArgument> //
        extends AbstractImmutableNormalizedNode<K, Iterable<DataContainerChild<? extends PathArgument, ?>>> //
        implements Immutable, DataContainerNode<K> {

    protected final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children;

    public AbstractImmutableDataContainerNode(
            final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children, final K nodeIdentifier) {
        super(nodeIdentifier);
        this.children = children;
    }

    @Override
    public final Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return Optional.<DataContainerChild<? extends PathArgument, ?>> fromNullable(children.get(child));
    }

    @Override
    public final Iterable<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return Iterables.unmodifiableIterable(children.values());
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    public final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> getChildren() {
        // Make sure we do not leak a mutable view
        return Collections.unmodifiableMap(children);
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        if (!(other instanceof AbstractImmutableDataContainerNode<?>)) {
            return false;
        }

        return children.equals(((AbstractImmutableDataContainerNode<?>)other).children);
    }
}
