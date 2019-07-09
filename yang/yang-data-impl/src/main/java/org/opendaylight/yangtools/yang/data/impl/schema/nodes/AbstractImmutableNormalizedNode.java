/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public abstract class AbstractImmutableNormalizedNode<K extends PathArgument, V> extends AbstractIdentifiable<K>
        implements NormalizedNode<K, V>, Immutable {
    protected AbstractImmutableNormalizedNode(final K nodeIdentifier) {
        super(nodeIdentifier);
    }

    @Override
    public final QName getNodeType() {
        return getIdentifier().getNodeType();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("value", getValue());
    }

    protected abstract boolean valueEquals(AbstractImmutableNormalizedNode<?, ?> other);

    protected abstract int valueHashCode();

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final AbstractImmutableNormalizedNode<?, ?> other = (AbstractImmutableNormalizedNode<?, ?>)obj;
        if (!getIdentifier().equals(other.getIdentifier())) {
            return false;
        }

        return valueEquals(other);
    }

    @Override
    public final int hashCode() {
        return 31 * getIdentifier().hashCode() + valueHashCode();
    }
}
