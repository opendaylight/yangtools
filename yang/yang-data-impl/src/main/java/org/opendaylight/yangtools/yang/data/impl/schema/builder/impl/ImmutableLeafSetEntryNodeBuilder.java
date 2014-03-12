/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Map;

public class ImmutableLeafSetEntryNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>>
        implements AttributesBuilder<ImmutableLeafSetEntryNodeBuilder<T>> {

    protected Map<QName, String> attributes = Collections.EMPTY_MAP;

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> create() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    public ImmutableLeafSetEntryNodeBuilder<T> withAttributes(Map<QName, String> attributes){
        this.attributes = attributes;
        return this;
    }

    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(nodeIdentifier, value, attributes);
    }

    static final class ImmutableLeafSetEntryNode<T> extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeWithValue, T> implements LeafSetEntryNode<T> {

        protected final Map<QName, String> attributes;

        ImmutableLeafSetEntryNode(InstanceIdentifier.NodeWithValue nodeIdentifier, T value, Map<QName, String> attributes) {
            super(nodeIdentifier, value);
            Preconditions.checkArgument(nodeIdentifier.getValue().equals(value),
                    "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
            Preconditions.checkNotNull(attributes);
            this.attributes = ImmutableMap.copyOf(attributes);
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ImmutableLeafSetEntryNode{");
            sb.append("nodeIdentifier=").append(nodeIdentifier);
            sb.append(", value=").append(value);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public Map<QName, String> getAttributes() {
            return attributes;
        }

        @Override
        public Object getAttributeValue(QName value) {
            return attributes.get(value);
        }
    }
}
