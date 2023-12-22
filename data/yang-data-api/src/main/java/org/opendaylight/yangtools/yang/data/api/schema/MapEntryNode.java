/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;

/**
 * Instance of Map entry, this node does not contains value, but child nodes.
 */
public non-sealed interface MapEntryNode extends DataContainerNode {
    @Override
    default Class<MapEntryNode> contract() {
        return MapEntryNode.class;
    }

    /**
     * Returns identifier of this node in parent map node
     *
     * <p>
     * Contents of identifier is defined by <code>key</code> (
     * {@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode#getKeyDefinition()}
     * ) statement in YANG schema for associated list item and child {@link LeafNode}s
     * values with {@link NodeIdentifier} as defined in the schema.
     *
     * @return identifier of this node in the context of parent node
     */
    @Override
    NodeIdentifierWithPredicates name();

    /**
     * A builder of {@link MapEntryNode}s.
     */
    interface Builder extends DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> {
        // Just a specialization
    }
}
