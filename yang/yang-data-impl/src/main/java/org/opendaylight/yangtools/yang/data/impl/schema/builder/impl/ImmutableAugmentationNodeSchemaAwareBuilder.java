/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;

public class ImmutableAugmentationNodeSchemaAwareBuilder extends ImmutableAugmentationNodeBuilder {

    private final DataNodeContainerValidator validator;

    protected ImmutableAugmentationNodeSchemaAwareBuilder(final AugmentationSchemaNode schema) {
        this.validator = new DataNodeContainerValidator(schema);
        super.withNodeIdentifier(DataSchemaContextNode.augmentationIdentifierFrom(schema));
    }

    @Override
    public DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> withNodeIdentifier(
            final AugmentationIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> withChild(
            final DataContainerChild<?, ?> child) {
        return super.withChild(validator.validateChild(child));
    }

    public static @NonNull DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> create(
            final AugmentationSchemaNode schema) {
        return new ImmutableAugmentationNodeSchemaAwareBuilder(schema);
    }
}
