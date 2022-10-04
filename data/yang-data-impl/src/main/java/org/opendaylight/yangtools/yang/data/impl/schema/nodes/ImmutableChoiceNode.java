/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;

public final class ImmutableChoiceNode
        extends AbstractImmutableDataContainerNode<NodeIdentifier, ChoiceNode> implements ChoiceNode {
    public ImmutableChoiceNode(final NodeIdentifier nodeIdentifier, final Map<PathArgument, Object> children) {
        super(children, nodeIdentifier);
    }

    @Override
    protected Class<ChoiceNode> implementedType() {
        return ChoiceNode.class;
    }
}