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
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract base class for {@link NormalizedNode} implementations.
 *
 * @param <T> Implemented {@link NormalizedNode} specialization type
 */
@Beta
public abstract class AbstractNormalizedNode<T extends NormalizedNode> implements NormalizedNode, Immutable {
    @Override
    public final PrettyTree prettyTree() {
        return new NormalizedNodePrettyTree(this);
    }

    @SuppressFBWarnings("EQ_UNUSUAL")
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final var clazz = implementedType();
        return clazz.isInstance(obj) && equalsImpl(clazz.cast(obj));
    }

    protected abstract boolean equalsImpl(@NonNull T other);

    @Override
    public final int hashCode() {
        return hashCodeImpl();
    }

    protected abstract int hashCodeImpl();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("body", body());
    }

    protected abstract @NonNull Class<T> implementedType();
}
