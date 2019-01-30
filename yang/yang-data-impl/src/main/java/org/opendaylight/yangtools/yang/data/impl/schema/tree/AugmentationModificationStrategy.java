/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Single;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationModificationStrategy
        extends AbstractDataNodeContainerModificationStrategy<AugmentationSchemaNode> {
    private static final Single<AugmentationIdentifier, AugmentationNode> SUPPORT = new Single<>(AugmentationNode.class,
            ImmutableAugmentationNodeBuilder::create, ImmutableAugmentationNodeBuilder::create);

    private AugmentationModificationStrategy(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        super(AugmentationNode.class, SUPPORT, EffectiveAugmentationSchema.create(schema, resolved), treeConfig);
    }

    static AutomaticLifecycleMixin of(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        return new AutomaticLifecycleMixin(new AugmentationModificationStrategy(schema, resolved, treeConfig),
            Builders.augmentationBuilder().withNodeIdentifier(DataSchemaContextNode.augmentationIdentifierFrom(schema))
            .build());
    }
}
