/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractMapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.spi.node.LazyLeafOperations;
import org.opendaylight.yangtools.yang.data.spi.node.LazyValues;

final class ImmutableMapEntryNode extends AbstractMapEntryNode {
    private final @NonNull NodeIdentifierWithPredicates name;
    final @NonNull Map<NodeIdentifier, Object> children;

    ImmutableMapEntryNode(final NodeIdentifierWithPredicates name, final Map<NodeIdentifier, Object> children) {
        this.name = requireNonNull(name);
        // FIXME: move this to caller
        this.children = ImmutableOffsetMap.unorderedCopyOf(children);
    }

    @Override
    public NodeIdentifierWithPredicates name() {
        return name;
    }

    @Override
    public DataContainerChild childByArg(final NodeIdentifier child) {
        return LazyLeafOperations.getChild(children, child);
    }

    @Override
    public Collection<DataContainerChild> body() {
        return new LazyValues(children);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final MapEntryNode other) {
        return other instanceof ImmutableMapEntryNode immutable ? children.equals(immutable.children)
            : ImmutableNormalizedNodeMethods.bodyEquals(this, other);
    }
}