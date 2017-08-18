/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

public class ImmutableLeafSetEntryNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> {

    public static <T> ImmutableLeafSetEntryNodeBuilder<T> create() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(getNodeIdentifier(), getValue(), getAttributes());
    }

    private static final class ImmutableLeafSetEntryNode<T> extends AbstractImmutableNormalizedValueAttrNode<NodeWithValue, T> implements LeafSetEntryNode<T> {

        ImmutableLeafSetEntryNode(final NodeWithValue nodeIdentifier, final T value, final Map<QName, String> attributes) {
            super(nodeIdentifier, value, attributes);
            checkArgument(Objects.deepEquals(nodeIdentifier.getValue(), value),
                    "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
        }
    }
}
