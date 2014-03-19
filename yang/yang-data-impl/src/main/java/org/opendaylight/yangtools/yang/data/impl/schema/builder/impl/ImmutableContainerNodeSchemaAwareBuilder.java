/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public final class ImmutableContainerNodeSchemaAwareBuilder extends ImmutableContainerNodeBuilder {

    private final DataNodeContainerValidator validator;
    // TODO remove schema aware builders, replace by validator called at build() ?

    private ImmutableContainerNodeSchemaAwareBuilder(ContainerSchemaNode schema) {
        this.validator = new DataNodeContainerValidator(schema);
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> create(ContainerSchemaNode schema) {
        return new ImmutableContainerNodeSchemaAwareBuilder(schema);
    }

    @Override
    public DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> withChild(DataContainerChild<?, ?> child) {
        validator.validateChild(child.getIdentifier());
        return super.withChild(child);
    }

    @Override
    public ContainerNode build() {
        // TODO check when statements... somewhere
        return super.build();
    }
}
