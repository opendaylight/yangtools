/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

import com.google.common.base.Optional;

public class ImmutableMapNode implements MapNode {

    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children;

    public ImmutableMapNode(InstanceIdentifier.NodeIdentifier nodeIdentifier,
            Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children) {
        this.nodeIdentifier = nodeIdentifier;
        this.children = children;
    }

    @Override
    public QName getNodeType() {
        return getIdentifier().getNodeType();
    }

    @Override
    public CompositeNode getParent() {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public InstanceIdentifier.NodeIdentifier getIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public QName getKey() {
        return getNodeType();
    }

    @Override
    public Iterable<MapEntryNode> getValue() {
        return children.values();
    }

    @Override
    public Iterable<MapEntryNode> setValue(Iterable<MapEntryNode> value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public Optional<MapEntryNode> getChild(InstanceIdentifier.NodeIdentifierWithPredicates child) {
        return Optional.fromNullable(children.get(child));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ImmutableMapNode{");
        sb.append("nodeIdentifier=").append(nodeIdentifier);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
