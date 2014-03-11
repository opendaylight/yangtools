/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public abstract class LeafSetEntryNodeBaseSerializer<E> implements
        FromNormalizedNodeSerializer<E, LeafSetEntryNode<?>, LeafListSchemaNode> {

    @Override
    public List<E> serialize(LeafListSchemaNode schema, LeafSetEntryNode<?> node) {
        return Collections.singletonList(serializeLeaf(schema, node));
    }

    protected abstract E serializeLeaf(LeafListSchemaNode schema, LeafSetEntryNode<?> node);
}
