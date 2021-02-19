/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableMapNode extends AbstractNormalizedNode<NodeIdentifier, SystemMapNode>
        implements SystemMapNode {

    final @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> children;

    public ImmutableMapNode(final NodeIdentifier nodeIdentifier,
            final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
        super(nodeIdentifier);
        this.children = requireNonNull(children);
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
    protected Class<SystemMapNode> implementedType() {
        return SystemMapNode.class;
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final SystemMapNode other) {
        final Map<NodeIdentifierWithPredicates, MapEntryNode> otherChildren =
            other instanceof ImmutableMapNode ? ((ImmutableMapNode) other).children : other.asMap();
        return children.equals(otherChildren);
    }
}