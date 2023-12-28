/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractUserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;

final class ImmutableUserMapNode extends AbstractUserMapNode {
    private final @NonNull NodeIdentifier name;
    final @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> children;

    ImmutableUserMapNode(final NodeIdentifier name,
            final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
        this.name = requireNonNull(name);
        this.children = requireNonNull(children);
    }

    @Override
    public NodeIdentifier name() {
        return name;
    }

    @Override
    public MapEntryNode childByArg(final NodeIdentifierWithPredicates child) {
        return children.get(child);
    }

    @Override
    public MapEntryNode childAt(final int position) {
        return Iterables.get(children.values(), position);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public Collection<MapEntryNode> value() {
        return children.values();
    }

    @Override
    public Collection<MapEntryNode> wrappedValue() {
        return UnmodifiableCollection.create(value());
    }

    @Override
    public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
        return UnmodifiableMap.of(children);
    }

    @Override
    protected int valueHashCode() {
        // Order is important
        int hashCode = 1;
        for (var child : children.values()) {
            hashCode = 31 * hashCode + child.hashCode();
        }
        return hashCode;
    }

    @Override
    protected boolean valueEquals(final UserMapNode other) {
        final var otherChildren = other instanceof ImmutableUserMapNode immutable ? immutable.children
            : other.asMap();
        return Iterables.elementsEqual(children.values(), otherChildren.values());
    }
}