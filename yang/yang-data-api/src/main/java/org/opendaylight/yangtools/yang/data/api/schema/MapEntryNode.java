/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 *
 * Instance of Map entry, this node does not contains value, but child nodes.
 *
 */
public interface MapEntryNode extends AttributesContainer, DataContainerNode<NodeIdentifierWithPredicates> {

    /**
     *
     * Returns identifier of this node in parent map node
     *
     * Contents of identifier is defined by <code>key</code> (
     * {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.ListSchemaNode#getKeyDefinition()}
     * ) statement in YANG schema for associated list item and child {@link LeafNode}s
     * values with {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
     * as defined in the schema.
     *
     * @return identifier of this node in the context of parent node
     */
    @Override
    NodeIdentifierWithPredicates getIdentifier();

}
