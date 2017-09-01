/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationModificationStrategy extends AbstractDataNodeContainerModificationStrategy<AugmentationSchema> {
    AugmentationModificationStrategy(final AugmentationSchema schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        super(createAugmentProxy(schema,resolved), AugmentationNode.class, treeConfig);
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

    private static AugmentationSchema createAugmentProxy(final AugmentationSchema schema,
            final DataNodeContainer resolved) {
        final Set<DataSchemaNode> realChildSchemas = new HashSet<>();
        for (final DataSchemaNode augChild : schema.getChildNodes()) {
            realChildSchemas.add(resolved.getDataChildByName(augChild.getQName()));
        }
        return new EffectiveAugmentationSchema(schema, realChildSchemas);
    }
}