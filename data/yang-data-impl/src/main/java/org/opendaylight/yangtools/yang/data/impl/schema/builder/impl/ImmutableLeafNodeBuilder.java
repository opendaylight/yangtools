/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractLeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

public final class ImmutableLeafNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>>
        implements LeafNode.Builder<T> {
    @Beta
    @SuppressWarnings("unchecked")
    public static <T> @NonNull LeafNode<T> createNode(final NodeIdentifier identifier, final T value) {
        if (value instanceof byte[] bytes) {
            return (LeafNode<T>) new ImmutableBinaryLeafNode(identifier, bytes);
        }
        return new ImmutableLeafNode<>(identifier, value);
    }

    @Override
    public LeafNode<T> build() {
        return createNode(getNodeIdentifier(), getValue());
    }

    private abstract static sealed class AbstractImmutableLeafNode<T> extends AbstractLeafNode<T> {
        private final @NonNull NodeIdentifier name;
        private final @NonNull T value;

        AbstractImmutableLeafNode(final NodeIdentifier name, final T value) {
            this.name = requireNonNull(name);
            this.value = requireNonNull(value);
        }

        @Override
        public final NodeIdentifier name() {
            return name;
        }

        @Override
        protected final T value() {
            return value;
        }
    }

    private static final class ImmutableLeafNode<T> extends AbstractImmutableLeafNode<T> {
        ImmutableLeafNode(final NodeIdentifier name, final T value) {
            super(name, value);
        }

        @Override
        protected T wrappedValue() {
            return value();
        }
    }

    private static final class ImmutableBinaryLeafNode extends AbstractImmutableLeafNode<byte[]> {
        ImmutableBinaryLeafNode(final NodeIdentifier name, final byte[] value) {
            super(name, value);
        }

        @Override
        protected byte[] wrappedValue() {
            return value().clone();
        }
    }
}
