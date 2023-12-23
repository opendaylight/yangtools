/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedSimpleValueNode;

public sealed class ImmutableLeafNode<T>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, LeafNode<T>, T>
        implements LeafNode<T> {
    private static final class ImmutableBinaryLeafNode extends ImmutableLeafNode<byte[]> {
        ImmutableBinaryLeafNode(final NodeIdentifier name, final byte[] value) {
            super(name, value);
        }

        @Override
        protected byte[] wrapValue(final byte[] valueToWrap) {
            return valueToWrap.clone();
        }
    }

    private ImmutableLeafNode(final NodeIdentifier name, final T value) {
        super(name, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NonNull LeafNode<T> of(final NodeIdentifier name, final T value) {
        return value instanceof byte[] bytes ? (LeafNode<T>) new ImmutableBinaryLeafNode(name, bytes)
            : new ImmutableLeafNode<>(name, value);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected final Class<LeafNode<T>> implementedType() {
        return (Class) LeafNode.class;
    }
}
