/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Preconditions;

public class ImmutableLeafSetEntryNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> {

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> create() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(nodeIdentifier, value);
    }

    static final class ImmutableLeafSetEntryNode<T> extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeWithValue, T> implements LeafSetEntryNode<T> {

        ImmutableLeafSetEntryNode(InstanceIdentifier.NodeWithValue nodeIdentifier, T value) {
            super(nodeIdentifier, value);
            Preconditions.checkArgument(nodeIdentifier.getValue().equals(value),
                    "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
        }

    }
}
