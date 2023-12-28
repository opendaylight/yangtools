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
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractSystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;

final class ImmutableSystemMapNode extends AbstractSystemMapNode {
    private final @NonNull NodeIdentifier name;
    final @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> children;

    ImmutableSystemMapNode(final NodeIdentifier name, final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
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
    public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
        return UnmodifiableMap.of(children);
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
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final SystemMapNode other) {
        return children.equals(ImmutableSystemMapNodeBuilder.accessChildren(other));
    }
}