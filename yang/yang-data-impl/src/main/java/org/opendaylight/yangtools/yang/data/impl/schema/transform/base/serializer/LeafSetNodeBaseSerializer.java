/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

/**
 * Abstract(base) serializer for LeafSetNodes, serializes elements of type E.
 *
 * @param <E> type of serialized elements
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class LeafSetNodeBaseSerializer<E> implements
        FromNormalizedNodeSerializer<E, LeafSetNode<?>, LeafListSchemaNode> {

    @Override
    public final Iterable<E> serialize(final LeafListSchemaNode schema, final LeafSetNode<?> node) {
        return Iterables.concat(Iterables.transform(node.getValue(), input -> {
            final Iterable<E> serializedChild = getLeafSetEntryNodeSerializer().serialize(schema, input);
            final int size = Iterables.size(serializedChild);
            Preconditions.checkState(size == 1,
                    "Unexpected count of elements for leaf-list entry serialized from: %s, should be 1, was: %s",
                    input, size);
            return serializedChild;
        }));
    }

    /**
     *
     * @return serializer for inner LeafSetEntryNodes used to serialize every entry of LeafSetNode, might be the same instance in case its immutable
     */
    protected abstract FromNormalizedNodeSerializer<E, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeSerializer();
}
