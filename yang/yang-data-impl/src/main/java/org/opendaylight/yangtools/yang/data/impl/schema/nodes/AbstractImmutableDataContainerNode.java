/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

public abstract class AbstractImmutableDataContainerNode<K extends PathArgument>
        extends AbstractImmutableNormalizedNode<K, Collection<DataContainerChild<? extends PathArgument, ?>>>
        implements Immutable, DataContainerNode<K> {
    private final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children;

    public AbstractImmutableDataContainerNode(
            final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children, final K nodeIdentifier) {
        super(nodeIdentifier);

        this.children = ImmutableOffsetMap.unorderedCopyOf(children);
    }

    @Override
    public final Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return Optional.ofNullable(children.get(child));
    }

    @Override
    public final Collection<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return children.values();
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    /**
     * DO NOT USE THIS METHOD.
     *
     * This is an implementation-internal API and no outside users should use it. If you do,
     * you are asking for trouble, as the returned object is not guaranteed to conform to
     * java.util.Map interface.
     *
     * @return An unmodifiable view if this node's children.
     */
    public final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> getChildren() {
        return children;
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        return other instanceof AbstractImmutableDataContainerNode<?> && children.equals(
                ((AbstractImmutableDataContainerNode<?>) other).children);

    }
}
