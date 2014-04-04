/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedAttrNode;

import com.google.common.base.Preconditions;

public class ImmutableLeafSetEntryNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> {

    public static <T> ImmutableLeafSetEntryNodeBuilder<T> create() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(getNodeIdentifier(), getValue(), getAttributes());
    }

    private static final class ImmutableLeafSetEntryNode<T> extends AbstractImmutableNormalizedAttrNode<InstanceIdentifier.NodeWithValue, T> implements LeafSetEntryNode<T> {

        ImmutableLeafSetEntryNode(final InstanceIdentifier.NodeWithValue nodeIdentifier, final T value, final Map<QName, String> attributes) {
            super(nodeIdentifier, value, attributes);
            Preconditions.checkArgument(nodeIdentifier.getValue().equals(value),
                    "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
        }
    }
}
