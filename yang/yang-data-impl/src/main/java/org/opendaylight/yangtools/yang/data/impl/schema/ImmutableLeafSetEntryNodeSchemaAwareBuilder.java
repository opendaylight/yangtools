/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class ImmutableLeafSetEntryNodeSchemaAwareBuilder<T> extends ImmutableLeafSetEntryNodeBuilder<T> {

    private final LeafListSchemaNode schema;

    private ImmutableLeafSetEntryNodeSchemaAwareBuilder(LeafListSchemaNode schema) {
        super();
        this.schema = schema;
    }

    public static <T> ImmutableLeafSetEntryNodeSchemaAwareBuilder<T> get(LeafListSchemaNode schema) {
        return new ImmutableLeafSetEntryNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public ImmutableLeafSetEntryNodeBuilder<T> withValue(T value) {
        super.withNodeIdentifier(new InstanceIdentifier.NodeWithValue(schema.getQName(), value));
        // TODO check value type
        return super.withValue(value);
    }

    @Override
    public ImmutableLeafSetEntryNodeBuilder<T> withNodeIdentifier(InstanceIdentifier.NodeWithValue nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

}
