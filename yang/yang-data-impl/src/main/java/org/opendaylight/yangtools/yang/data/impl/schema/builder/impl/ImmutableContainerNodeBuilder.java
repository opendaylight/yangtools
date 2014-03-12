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
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

import java.util.Collections;
import java.util.Map;

public class ImmutableContainerNodeBuilder extends AbstractImmutableDataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> {

    protected Map<QName, String> attributes = Collections.EMPTY_MAP;

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> create() {
        return new ImmutableContainerNodeBuilder();
    }

    public ImmutableContainerNodeBuilder withAttributes(Map<QName, String> attributes){
        this.attributes = attributes;
        return this;
    }

    @Override
    public ContainerNode build() {
        return new ImmutableContainerNode(nodeIdentifier, value, attributes);
    }

    final class ImmutableContainerNode
            extends AbstractImmutableDataContainerNode<InstanceIdentifier.NodeIdentifier>
            implements ContainerNode {

        protected Map<QName, String> attributes;

        ImmutableContainerNode(
                InstanceIdentifier.NodeIdentifier nodeIdentifier,
                Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children, Map<QName, String> attributes) {
            super(children, nodeIdentifier);
            Preconditions.checkNotNull(attributes);
            this.attributes = ImmutableMap.copyOf(attributes);
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
