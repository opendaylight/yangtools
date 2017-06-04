/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Containment node, which contains {@link MapEntryNode} of the same type, which may
 * be quickly retrieved using key.
 *
 * <p>
 * This node maps to the list node in YANG schema, schema and semantics of this node,
 * its children and key construction is  defined by YANG <code>list</code>
 * statement and its <code>key</code> and <code>ordered-by</code> substatements.
 */
public interface MapNode extends MixinNode, DataContainerChild<NodeIdentifier, Collection<MapEntryNode>>,
        NormalizedNodeContainer<NodeIdentifier, NodeIdentifierWithPredicates, MapEntryNode> {

}
