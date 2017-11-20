/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Context wrapper around a {@link NormalizedNode} for use with Jaxen. It tracks the parent node for purposes of
 * traversing upwards the NormalizedNode tree.
 */
final class NormalizedNodeContext extends Context implements Function<NormalizedNode<?, ?>, NormalizedNodeContext> {
    private static final long serialVersionUID = 1L;
    private final NormalizedNodeContext parent;
    private final NormalizedNode<?, ?> node;

    NormalizedNodeContext(@Nonnull final ContextSupport contextSupport, @Nonnull final NormalizedNode<?, ?> node,
        @Nullable final NormalizedNodeContext parent) {
        super(contextSupport);
        this.node = requireNonNull(node);
        this.parent = parent;

        setNodeSet(ImmutableList.of(this));
    }

    @Nonnull NormalizedNode<?, ?> getNode() {
        return node;
    }

    @Nullable NormalizedNodeContext getParent() {
        return parent;
    }

    @Nonnull YangInstanceIdentifier getPath() {
        return (parent == null ? YangInstanceIdentifier.EMPTY : parent.getPath()).node(node.getIdentifier());
    }

    @Override
    public NormalizedNodeContext apply(final NormalizedNode<?, ?> input) {
        return new NormalizedNodeContext(getContextSupport(), input, this);
    }
}
