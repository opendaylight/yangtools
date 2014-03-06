/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

import com.google.common.base.Preconditions;

final class ImmutableLeafSetEntryNode<T> implements LeafSetEntryNode<T> {

    private InstanceIdentifier.NodeWithValue nodeIdentifier;
    private T value;

    public ImmutableLeafSetEntryNode(InstanceIdentifier.NodeWithValue nodeIdentifier, T value) {
        this.nodeIdentifier = Preconditions.checkNotNull(nodeIdentifier, "nodeIdentifier");
        this.value = Preconditions.checkNotNull(value, "value");
        Preconditions.checkArgument(nodeIdentifier.getValue().equals(value),
                "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
    }

    @Override
    public QName getNodeType() {
        return nodeIdentifier.getNodeType();
    }

    @Override
    public CompositeNode getParent() {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public InstanceIdentifier.NodeWithValue getIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public QName getKey() {
        return getNodeType();
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableLeafSetEntryNode)) return false;

        ImmutableLeafSetEntryNode that = (ImmutableLeafSetEntryNode) o;

        if (!nodeIdentifier.equals(that.nodeIdentifier)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeIdentifier.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ImmutableLeafSetEntryNode{");
        sb.append("nodeIdentifier=").append(nodeIdentifier);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
