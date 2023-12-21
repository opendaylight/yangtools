/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;

/**
 * Containment node, which contains {@link UnkeyedListEntryNode} of the same type, which may be quickly retrieved using
 * key. This node maps to the <code>list</code> statement in YANG schema, which did not define {@code key} substatement.
 *
 * <p>
 * Ordering of the elements is user-defined during construction of instance of this interface. Ordered view of elements
 * (iteration) is provided by {@link #body()} call.
 */
public non-sealed interface UnkeyedListNode
        extends OrderedNodeContainer<UnkeyedListEntryNode>, DataContainerChild, MixinNode {
    @Override
    default Class<UnkeyedListNode> contract() {
        return UnkeyedListNode.class;
    }

    /**
     * A builder of {@link UnkeyedListNode}s.
     */
    interface Builder extends CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> {
        /**
         * Return the resulting {@link SystemMapNode}.
         *
         * @return resulting {@link SystemMapNode}
         * @throws IllegalStateException if this builder does not have sufficient state
         */
        @NonNull UnkeyedListNode build();
    }
}
