/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public abstract class LeafSetNodeBaseSerializer<E> implements
        FromNormalizedNodeSerializer<E, LeafSetNode<?>, LeafListSchemaNode> {

    @Override
    public List<E> serialize(LeafListSchemaNode schema, LeafSetNode<?> node) {
        List<E> elements = Lists.newArrayList();

        for (LeafSetEntryNode<?> leafSetEntryNode : node.getValue()) {
            List<E> serializedChild = getLeafSetEntryNodeSerializer().serialize(schema, leafSetEntryNode);
            Preconditions.checkState(serializedChild.size() == 1,
                    "Unexpected count of elements for leaf-list entry serialized from: %s, should be 1, was: %s",
                    leafSetEntryNode, serializedChild.size());
            elements.addAll(serializedChild);
        }

        return elements;
    }

    protected abstract FromNormalizedNodeSerializer<E, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeSerializer();
}
