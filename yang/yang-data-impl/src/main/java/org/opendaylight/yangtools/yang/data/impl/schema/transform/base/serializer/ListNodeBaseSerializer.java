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
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Abstract(base) serializer for ListNodes (MapNode, UnkeyedListNode), serializes elements of type E.
 *
 * @param <E>
 *            type of serialized elements
 * @param <N>
 *            containing node type
 * @param <O>
 *            entry node type which is inside containing (N) type
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class ListNodeBaseSerializer<E, N extends DataContainerChild<NodeIdentifier, Collection<O>>, O extends DataContainerNode<?>>
        implements FromNormalizedNodeSerializer<E, N, ListSchemaNode> {

    @Override
    public final Iterable<E> serialize(final ListSchemaNode schema, final N node) {
        return Iterables.concat(Iterables.transform(node.getValue(), input -> {
            final Iterable<E> serializedChild = getListEntryNodeSerializer().serialize(schema, input);
            final int size = Iterables.size(serializedChild);

            Preconditions.checkState(size == 1,
                    "Unexpected count of entries  for list serialized from: %s, should be 1, was: %s", input, size);
            return serializedChild;
        }));
    }

    /**
     *
     * @return serializer for inner ListEntryNodes (MapEntryNode, UnkeyedListEntryNode) used to serialize every entry of
     *         ListNode, might be the same instance in case its immutable
     */
    protected abstract FromNormalizedNodeSerializer<E, O, ListSchemaNode> getListEntryNodeSerializer();
}
