/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public abstract class AbstractImmutableNormalizedNode<K extends PathArgument, N extends NormalizedNode>
        extends AbstractIdentifiable<PathArgument, K> implements NormalizedNode, Immutable {
    protected AbstractImmutableNormalizedNode(final K nodeIdentifier) {
        super(nodeIdentifier);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final Class<N> clazz = implementedType();
        if (!clazz.isInstance(obj)) {
            return false;
        }
        final N other = clazz.cast(obj);
        return getIdentifier().equals(other.getIdentifier()) && valueEquals(other);
    }

    @Override
    public final int hashCode() {
        return 31 * getIdentifier().hashCode() + valueHashCode();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("body", body());
    }

    protected abstract @NonNull Class<N> implementedType();

    protected abstract int valueHashCode();
    
    protected abstract boolean valueEquals(@NonNull N other);
}
