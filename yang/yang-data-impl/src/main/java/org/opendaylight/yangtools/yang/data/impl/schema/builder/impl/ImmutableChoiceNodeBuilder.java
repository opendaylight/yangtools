/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

public class ImmutableChoiceNodeBuilder extends AbstractImmutableDataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> {

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> create() {
        return new ImmutableChoiceNodeBuilder();
    }

    public ChoiceNode build() {
        return new ImmutableChoiceNode(nodeIdentifier, value);
    }

    static final class ImmutableChoiceNode
            extends AbstractImmutableDataContainerNode<InstanceIdentifier.NodeIdentifier>
            implements ChoiceNode {

        ImmutableChoiceNode(InstanceIdentifier.NodeIdentifier nodeIdentifier,
                            Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children) {
            super(children, nodeIdentifier);
        }

    }
}
