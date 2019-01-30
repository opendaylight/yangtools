/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationModificationStrategy
        extends AbstractDataNodeContainerModificationStrategy<AugmentationSchemaNode> {
    private AugmentationModificationStrategy(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        super(EffectiveAugmentationSchema.create(schema, resolved), AugmentationNode.class, treeConfig);
    }

    static AutomaticLifecycleMixin of(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        return new AutomaticLifecycleMixin(new AugmentationModificationStrategy(schema, resolved, treeConfig),
            Builders.augmentationBuilder().withNodeIdentifier(DataSchemaContextNode.augmentationIdentifierFrom(schema))
            .build());
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof AugmentationNode);
        return ImmutableAugmentationNodeBuilder.create((AugmentationNode) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof AugmentationNode);
        return ImmutableAugmentationNodeBuilder.create()
                .withNodeIdentifier(((AugmentationNode) original).getIdentifier()).build();
    }
}
