/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;

public abstract class AbstractImmutableNormalizedNode<K extends InstanceIdentifier.PathArgument,V>
        implements NormalizedNode<K, V>, Immutable {

    private final K nodeIdentifier;
    private final V value;

    protected AbstractImmutableNormalizedNode(final K nodeIdentifier, final V value) {
        this.nodeIdentifier = Preconditions.checkNotNull(nodeIdentifier, "nodeIdentifier");
        this.value = Preconditions.checkNotNull(value, "value");
    }

    @Override
    public final QName getNodeType() {
        return getIdentifier().getNodeType();
    }

    @Override
    public final K getIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public final CompositeNode getParent() {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public final QName getKey() {
        return getNodeType();
    }

    @Override
    public final V getValue() {
        return value;
    }

    @Override
    public final V setValue(final V value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public final String toString() {
        return addToStringAttributes(Objects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("nodeIdentifier", nodeIdentifier).add("value", getValue());
    }

    protected abstract boolean valueEquals(NormalizedNode<?, ?> other);
    protected abstract int valueHashCode();

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NormalizedNode<?, ?>)) {
            return false;
        }

        final NormalizedNode<?, ?> other = (NormalizedNode<?, ?>)obj;
        if (!nodeIdentifier.equals(other.getIdentifier())) {
            return false;
        }

        return valueEquals(other);
    }

    @Override
    public final int hashCode() {
        int result = nodeIdentifier.hashCode();
        result = 31 * result + valueHashCode();
        return result;
    }
}
