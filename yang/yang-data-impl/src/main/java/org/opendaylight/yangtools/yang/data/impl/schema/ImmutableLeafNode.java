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
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

import com.google.common.base.Preconditions;

final class ImmutableLeafNode<T> implements LeafNode<T> {

    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private T value;

    ImmutableLeafNode(InstanceIdentifier.NodeIdentifier nodeIdentifier, T value) {
        this.nodeIdentifier = Preconditions.checkNotNull(nodeIdentifier, "nodeIdentifier");
        this.value = Preconditions.checkNotNull(value, "value");
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
    public InstanceIdentifier.NodeIdentifier getIdentifier() {
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
        if (!(o instanceof ImmutableLeafNode)) return false;

        ImmutableLeafNode that = (ImmutableLeafNode) o;

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
        final StringBuffer sb = new StringBuffer("ImmutableLeafNode{");
        sb.append("nodeIdentifier=").append(nodeIdentifier);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
