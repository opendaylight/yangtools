/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableUserMapNode
        extends AbstractNormalizedNode<NodeIdentifier, UserMapNode> implements UserMapNode {
    final Map<NodeIdentifierWithPredicates, MapEntryNode> children;

    public ImmutableUserMapNode(final NodeIdentifier nodeIdentifier,
                     final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
        super(nodeIdentifier);
        this.children = children;
    }

    @Override
    public MapEntryNode childByArg(final NodeIdentifierWithPredicates child) {
        return children.get(child);
    }

    @Override
    public MapEntryNode getChild(final int position) {
        return Iterables.get(children.values(), position);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public Collection<MapEntryNode> body() {
        return UnmodifiableCollection.create(children.values());
    }

    @Override
    public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
        return UnmodifiableMap.of(children);
    }

    @Override
    protected Class<UserMapNode> implementedType() {
        return UserMapNode.class;
    }

    @Override
    protected int valueHashCode() {
        // Order is important
        int hashCode = 1;
        for (MapEntryNode child : children.values()) {
            hashCode = 31 * hashCode + child.hashCode();
        }
        return hashCode;
    }

    @Override
    protected boolean valueEquals(final UserMapNode other) {
        final Map<NodeIdentifierWithPredicates, MapEntryNode> otherChildren;
        if (other instanceof ImmutableUserMapNode) {
            otherChildren = ((ImmutableUserMapNode) other).children;
        } else {
            otherChildren = other.asMap();
        }
        return Iterables.elementsEqual(children.values(), otherChildren.values());
    }
}