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
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

public class ImmutableChoiceNode extends AbstractDataContainerNode<InstanceIdentifier.NodeIdentifier> implements ChoiceNode {

    private final InstanceIdentifier.NodeIdentifier nodeIdentifier;

    public ImmutableChoiceNode(InstanceIdentifier.NodeIdentifier nodeIdentifier,
            Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        super(children);
        this.nodeIdentifier = nodeIdentifier;
    }

    @Override
    public QName getNodeType() {
        return getIdentifier().getNodeType();
    }

    @Override
    public InstanceIdentifier.NodeIdentifier getIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public QName getKey() {
        return getNodeType();
    }
}
