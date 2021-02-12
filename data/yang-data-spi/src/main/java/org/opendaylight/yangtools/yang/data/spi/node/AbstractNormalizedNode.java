/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2020 PANTHEON.tech, s.r.o
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract base class for {@link NormalizedNode} implementations.
 *
 * @param <I> Identifier type
 * @param <T> Implemented {@link NormalizedNode} specialization type
 */
@Beta
public abstract class AbstractNormalizedNode<I extends PathArgument, T extends NormalizedNode>
        extends AbstractIdentifiable<PathArgument, I> implements NormalizedNode, Immutable {
    protected AbstractNormalizedNode(final I identifier) {
        super(identifier);
    }

    @Override
    public final PrettyTree prettyTree() {
        return new NormalizedNodePrettyTree(this);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final Class<T> clazz = implementedType();
        if (!clazz.isInstance(obj)) {
            return false;
        }
        final T other = clazz.cast(obj);
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

    protected abstract @NonNull Class<T> implementedType();

    protected abstract int valueHashCode();

    protected abstract boolean valueEquals(@NonNull T other);
}
