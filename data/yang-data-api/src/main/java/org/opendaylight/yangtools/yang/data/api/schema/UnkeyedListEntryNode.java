/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;

/**
 * List entry node, which does not have value, but child nodes. Represents an instance of data, which schema is instance
 * of {@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode} with key undefined.
 *
 * <p>
 * This nodes itself does not contain any ordering information, user supplied ordering is preserved by parent node,
 * which is an instance of {@link UnkeyedListNode}.
 */
public non-sealed interface UnkeyedListEntryNode extends DataContainerNode {
    @Override
    default Class<UnkeyedListEntryNode> contract() {
        return UnkeyedListEntryNode.class;
    }

    @Override
    NodeIdentifier name();

    /**
     * A builder of {@link UnkeyedListNode}s.
     */
    interface Builder extends DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> {
        // Just a specialization
    }
}
