/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedSimpleValueNode;

// FIXME: invert encapsulation: ImmutableLeafSetEntryNode<T> should be the outer class
public final class ImmutableLeafSetEntryNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeWithValue<T>, T, LeafSetEntryNode<T>>
        implements LeafSetEntryNode.Builder<T> {
    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(getNodeIdentifier(), getValue());
    }

    private static final class ImmutableLeafSetEntryNode<T>
            extends AbstractImmutableNormalizedSimpleValueNode<NodeWithValue<T>, LeafSetEntryNode<?>, T>
            implements LeafSetEntryNode<T> {

        ImmutableLeafSetEntryNode(final NodeWithValue<T> nodeIdentifier, final T value) {
            super(nodeIdentifier, value);
            checkArgument(Objects.deepEquals(nodeIdentifier.getValue(), value),
                    "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
        }

        @Override
        protected Class<LeafSetEntryNode<?>> implementedType() {
            return (Class) LeafSetEntryNode.class;
        }
    }
}
