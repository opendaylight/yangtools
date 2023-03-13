/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;

abstract class AbstractImmutableNormalizedNodeBuilder<I extends PathArgument, V, R extends NormalizedNode>
        implements NormalizedNodeBuilder<I, V, R> {
    private @Nullable I nodeIdentifier = null;
    private @Nullable V value = null;

    protected final I getNodeIdentifier() {
        return checkSet(nodeIdentifier, "Identifier has not been set");
    }

    protected final V getValue() {
        return checkSet(value, "Value has not been set");
    }

    @Override
    public NormalizedNodeBuilder<I, V, R> withValue(final V withValue) {
        this.value = checkValue(withValue);
        return this;
    }

    @Override
    public NormalizedNodeBuilder<I, V, R> withNodeIdentifier(final I withNodeIdentifier) {
        this.nodeIdentifier = requireNonNull(withNodeIdentifier);
        return this;
    }

    private static <T> @NonNull T checkSet(final @Nullable T obj, final String message) {
        if (obj == null) {
            throw new IllegalStateException(message);
        }
        return obj;
    }

    protected static final <T> @NonNull T checkValue(final @Nullable T value) {
        final var nonNull = requireNonNull(value);
        if (nonNull instanceof YangInstanceIdentifier yiid && yiid.isEmpty()) {
            throw new IllegalArgumentException("Node value cannot be an empty instance identifier");
        }
        return nonNull;
    }
}
