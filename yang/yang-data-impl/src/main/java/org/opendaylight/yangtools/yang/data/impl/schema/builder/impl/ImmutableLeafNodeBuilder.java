/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import java.util.Collections;
import java.util.Map;

public class ImmutableLeafNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> {

    protected Map<QName, String> attributes = Collections.EMPTY_MAP;

    protected ImmutableLeafNodeBuilder() {
    }

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> create() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    public LeafNode<T> build() {
        return new ImmutableLeafNode<>(nodeIdentifier, value, attributes);
    }

    public ImmutableLeafNodeBuilder<T> withAttributes(Map<QName, String> attributes){
        this.attributes = attributes;
        return this;
    }

    static final class ImmutableLeafNode<T> extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, T> implements LeafNode<T> {

        private final Map<QName, String> attributes;

        ImmutableLeafNode(InstanceIdentifier.NodeIdentifier nodeIdentifier, T value, Map<QName, String> attributes) {
            super(nodeIdentifier, value);
            Preconditions.checkNotNull(attributes);
            this.attributes = ImmutableMap.copyOf(attributes);

        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ImmutableLeafNode{");
            sb.append("nodeIdentifier=").append(nodeIdentifier);
            sb.append(", value=").append(value);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public Map<QName, String> getAttributes() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getAttributeValue(QName value) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
