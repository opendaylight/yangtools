/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public class ImmutableLeafNodeSchemaAwareBuilder<T> extends ImmutableLeafNodeBuilder<T> {

    private final LeafSchemaNode schema;

    private ImmutableLeafNodeSchemaAwareBuilder(LeafSchemaNode schema) {
        super();
        this.schema = schema;
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static <T> ImmutableLeafNodeSchemaAwareBuilder<T> get(LeafSchemaNode schema) {
        return new ImmutableLeafNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public ImmutableLeafNodeBuilder<T> withValue(T value) {
        // TODO check value type
        return super.withValue(value);
    }

    @Override
    public ImmutableLeafNodeBuilder<T> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
