/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableLeafNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> {

    protected ImmutableLeafNodeBuilder() {
    }

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> create() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    public LeafNode<T> build() {
        return new ImmutableLeafNode<>(nodeIdentifier, value);
    }

    static final class ImmutableLeafNode<T> extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, T> implements LeafNode<T> {

        ImmutableLeafNode(InstanceIdentifier.NodeIdentifier nodeIdentifier, T value) {
            super(nodeIdentifier, value);
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ImmutableLeafNode{");
            sb.append("nodeIdentifier=").append(nodeIdentifier);
            sb.append(", value=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }
}
