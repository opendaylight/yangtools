/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2020 PANTHEON.tech, s.r.o
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract base class for {@link NormalizedNode} implementations.
 *
 * @param <I> Identifier type
 * @param <T> Implemented {@link NormalizedNode} specialization type
 */
@Beta
public abstract class AbstractNormalizedNode<I extends PathArgument, T extends NormalizedNode>
        implements NormalizedNode, Immutable {
    private final @NonNull I name;

    protected AbstractNormalizedNode(final I name) {
        this.name = requireNonNull(name);
    }

    @Override
    public final I name() {
        return name;
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
        final var clazz = implementedType();
        if (!clazz.isInstance(obj)) {
            return false;
        }
        final var other = clazz.cast(obj);
        return name().equals(other.name()) && valueEquals(other);
    }

    @Override
    public final int hashCode() {
        return 31 * name().hashCode() + valueHashCode();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(toStringClass())).toString();
    }

    /**
     * Return the {@link #toString()} class identity of this object. Default implementation defers to
     * {@link #getClass()}. Override may be needed to hide implementation-internal class hierarchy -- for example if
     * providing different implementations of {@link LeafNode} for {@code String} and {@code byte[]} values.
     *
     * @return the {@link #toString()} class identity of this object
     */
    protected @NonNull Class<?> toStringClass() {
        return getClass();
    }

    protected @NonNull Object toStringBody() {
        return body();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("name", name()).add("body", toStringBody());
    }

    protected abstract @NonNull Class<T> implementedType();

    protected abstract int valueHashCode();

    protected abstract boolean valueEquals(@NonNull T other);
}
