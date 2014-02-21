/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Containment node, which contains {@link MapEntryNode} of the same type.
 *
 * This node maps to the list node in YANG schema.
 *
 */
public interface MapNode extends //
        MixinNode,
        DataContainerChild<NodeIdentifier, Iterable<MapEntryNode>>,
        NormalizedNodeContainer<NodeIdentifier, NodeIdentifierWithPredicates, MapEntryNode> {

    @Override
    public NodeIdentifier getIdentifier();




}
