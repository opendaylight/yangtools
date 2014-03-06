/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;

import com.google.common.base.Optional;

final class ImmutableLeafSetNode<T> implements LeafSetNode<T> {

    private final InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private final List<LeafSetEntryNode<T>> children;

    public ImmutableLeafSetNode(InstanceIdentifier.NodeIdentifier nodeIdentifier, List<LeafSetEntryNode<T>> children) {
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
    public Iterable<LeafSetEntryNode<T>> getValue() {
        return children;
    }

    @Override
    public Iterable<LeafSetEntryNode<T>> setValue(Iterable<LeafSetEntryNode<T>> value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public Optional<LeafSetEntryNode<T>> getChild(InstanceIdentifier.NodeWithValue child) {
        // FIXME
        for (LeafSetEntryNode<T> currentChild : children) {
            if(currentChild.getIdentifier().equals(child))
                return Optional.of(currentChild);
        }

        return Optional.absent();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ImmutableLeafSetNode{");
        sb.append("nodeIdentifier=").append(nodeIdentifier);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
