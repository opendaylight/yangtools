/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

public class ImmutableAugmentationNode extends AbstractDataContainerNode<InstanceIdentifier.AugmentationIdentifier> implements AugmentationNode {

    // TODO almost same as container, only different type of nodeIdentifier

    private InstanceIdentifier.AugmentationIdentifier nodeIdentifier;

    public ImmutableAugmentationNode(InstanceIdentifier.AugmentationIdentifier nodeIdentifier, Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        super(children);
        this.nodeIdentifier = nodeIdentifier;
    }

    @Override
    public QName getKey() {
        return getNodeType();
    }

    @Override
    public QName getNodeType() {
        return getIdentifier().getNodeType();
    }

    @Override
    public InstanceIdentifier.AugmentationIdentifier getIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ImmutableAugmentationNode{");
        sb.append("nodeIdentifier=").append(nodeIdentifier);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
