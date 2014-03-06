/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

import com.google.common.collect.Lists;

public class ImmutableLeafSetNodeBuilder<T> {
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private List<LeafSetEntryNode<T>> children;

    protected ImmutableLeafSetNodeBuilder() {
    }

    public static <T> ImmutableLeafSetNodeBuilder<T> get() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    public ImmutableLeafSetNodeBuilder<T> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableLeafSetNodeBuilder<T> withChild(ImmutableLeafSetEntryNode<T> child) {
        if(this.children == null) {
            this.children = Lists.newLinkedList();
        }

        this.children.add(child);
        return this;
    }

    public ImmutableLeafSetNode<T> build() {
        return new ImmutableLeafSetNode<>(nodeIdentifier, children);
    }

    public ImmutableLeafSetNodeBuilder<T> withChild(T value) {
        withChild(new ImmutableLeafSetEntryNode<>(
                new InstanceIdentifier.NodeWithValue(nodeIdentifier.getNodeType(), value), value));
        return this;
    }
}
