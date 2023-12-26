/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * Leaf node with multiplicity 0..1.
 *
 * <p>
 * Leaf node has a value, but no child nodes in the data tree, schema
 * for leaf node and its value is described by {@link org.opendaylight.yangtools.yang.model.api.LeafSchemaNode}.
 *
 * @param <T> Value type
 */
// FIXME: define specializations for each concrete normalized type:
//        Uint{8,16,32,64)
//        String
//        Byte,Short,Integer,Long
//        Empty
//        Set<String> (== bits)
//        YangInstanceIdentifier
//        QName (== identityref)
public non-sealed interface LeafNode<T> extends ValueNode<T>, DataContainerChild {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<LeafNode> contract() {
        return LeafNode.class;
    }

    /**
     * A builder of {@link LeafNode}s.
     */
    interface Builder<V> extends NormalizedNode.Builder {

        @NonNull Builder<V> withName(NodeIdentifier name);

        default @NonNull Builder<V> withName(final QName qname) {
            return withName(new NodeIdentifier(qname));
        }

        @NonNull Builder<V> withBody(V value);

        @Override
        LeafNode<V> build();
    }
}
