/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;


/**
 *
 * Containment node, which contains {@link UnkeyedListEntryNode} of the same type, which may
 * be quickly retrieved using key.
 *
 * This node maps to the <code>list</code> statement in YANG schema,
 * which did not define <code>key</code> substatement.
 *
 */
public interface UnkeyedListNode extends
    DataContainerChild<NodeIdentifier, Iterable<UnkeyedListEntryNode>>,
    OrderedNodeContainer<UnkeyedListEntryNode> {

}
