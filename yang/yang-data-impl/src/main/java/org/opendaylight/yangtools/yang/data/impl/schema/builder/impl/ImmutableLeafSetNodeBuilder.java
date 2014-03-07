/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class ImmutableLeafSetNodeBuilder<T>
        implements ListNodeBuilder<T, LeafSetEntryNode<T>> {

    protected Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> value;
    protected InstanceIdentifier.NodeIdentifier nodeIdentifier;

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(LeafSetEntryNode<T> child) {
        if(this.value == null) {
            this.value = Maps.newLinkedHashMap();
        }

        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public LeafSetNode<T> build() {
        return new ImmutableLeafSetNode<>(nodeIdentifier, value);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withValue(List<LeafSetEntryNode<T>> value) {
        for (LeafSetEntryNode<T> leafSetEntry : value) {
            withChild(leafSetEntry);
        }

        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(T value) {
        return withChild(new ImmutableLeafSetEntryNodeBuilder.ImmutableLeafSetEntryNode<>(new InstanceIdentifier.NodeWithValue(nodeIdentifier.getNodeType(), value), value));
    }

    final class ImmutableLeafSetNode<T> extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<LeafSetEntryNode<T>>> implements LeafSetNode<T> {

        private final Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> mappedChildren;

        ImmutableLeafSetNode(InstanceIdentifier.NodeIdentifier nodeIdentifier, Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> children) {
            super(nodeIdentifier, children.values());
            this.mappedChildren = children;
        }

        @Override
        public Optional<LeafSetEntryNode<T>> getChild(InstanceIdentifier.NodeWithValue child) {
            return Optional.fromNullable(mappedChildren.get(child));
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ImmutableLeafSetNode{");
            sb.append("nodeIdentifier=").append(nodeIdentifier);
            sb.append(", children=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }

}
