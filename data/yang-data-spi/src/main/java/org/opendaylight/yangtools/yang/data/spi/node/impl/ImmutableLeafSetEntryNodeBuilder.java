/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractLeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

public final class ImmutableLeafSetEntryNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeWithValue<T>, T, LeafSetEntryNode<T>>
        implements LeafSetEntryNode.Builder<T> {
    @Override
    public LeafSetEntryNode<T> build() {
        return AbstractImmutableLeafSetEntryNode.of(getNodeIdentifier(), getValue());
    }

    private abstract static sealed class AbstractImmutableLeafSetEntryNode<T> extends AbstractLeafSetEntryNode<T> {
        private final @NonNull NodeWithValue<T> name;

        AbstractImmutableLeafSetEntryNode(final NodeWithValue<T> name) {
            this.name = requireNonNull(name);
        }

        static <T> @NonNull AbstractImmutableLeafSetEntryNode<T> of(final NodeWithValue<T> name, final T body) {
            final var nameValue = name.getValue();
            if (body instanceof byte[] bodyBytes) {
                if (nameValue instanceof byte[] nameBytes && Arrays.equals(nameBytes, bodyBytes)) {
                    @SuppressWarnings("unchecked")
                    final var ret = (AbstractImmutableLeafSetEntryNode<T>)
                        new ImmutableBinaryLeafSetEntryNode((NodeWithValue<byte[]>) name);
                    return ret;
                }
            } else if (nameValue.equals(body)) {
                return new ImmutableLeafSetEntryNode<>(name);
            }

            throw new IllegalArgumentException(
                "Node identifier contains different value: " + name + " than value itself: " + body);
        }

        @Override
        public final NodeWithValue<T> name() {
            return name;
        }

        @Override
        protected final T value() {
            return name.getValue();
        }
    }

    private static final class ImmutableLeafSetEntryNode<T> extends AbstractImmutableLeafSetEntryNode<T> {
        ImmutableLeafSetEntryNode(final NodeWithValue<T> name) {
            super(name);
        }

        @Override
        protected T wrappedValue() {
            return value();
        }
    }

    private static final class ImmutableBinaryLeafSetEntryNode extends AbstractImmutableLeafSetEntryNode<byte[]> {
        ImmutableBinaryLeafSetEntryNode(final NodeWithValue<byte[]> name) {
            super(name);
        }

        @Override
        protected byte[] wrappedValue() {
            return value().clone();
        }
    }
}
