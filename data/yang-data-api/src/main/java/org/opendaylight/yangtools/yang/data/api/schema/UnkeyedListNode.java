/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * Containment node, which contains {@link UnkeyedListEntryNode} of the same type, which may be quickly retrieved using
 * key. This node maps to the <code>list</code> statement in YANG schema, which did not define {@code key} substatement.
 *
 * <p>
 * Ordering of the elements is user-defined during construction of instance of this interface. Ordered view of elements
 * (iteration) is provided by {@link #body()} call.
 */
public interface UnkeyedListNode extends OrderedNodeContainer<UnkeyedListEntryNode>, DataContainerChild {
    @Override
    NodeIdentifier getIdentifier();

    @Override
    default Class<UnkeyedListNode> contract() {
        return UnkeyedListNode.class;
    }
}
