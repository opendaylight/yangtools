/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;

/**
 * Node representing data instance of <code>choice</code>.
 *
 * <p>
 * Choice node is instance of one of possible alternatives, from which only one is allowed to exist at one time in
 * particular context of parent node.
 *
 * <p>
 * YANG Model and schema for choice is described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode}.
 *
 * <p>
 * Valid alternatives of subtree are described by instances of
 * {@link org.opendaylight.yangtools.yang.model.api.CaseSchemaNode}, which are retrieved via
 * {@link org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode#getCases()}.
 */
public non-sealed interface ChoiceNode extends DataContainerNode, DataContainerChild, MixinNode {
    @Override
    default Class<ChoiceNode> contract() {
        return ChoiceNode.class;
    }

    /**
     * A builder of {@link ChoiceNode}s.
     */
    interface Builder extends DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> {
        // Just a specialization
    }
}
