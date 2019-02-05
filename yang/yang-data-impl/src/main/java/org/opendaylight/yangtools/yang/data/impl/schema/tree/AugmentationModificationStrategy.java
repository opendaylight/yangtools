/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationModificationStrategy
        extends DataNodeContainerModificationStrategy<AugmentationSchemaNode> {
    private static final NormalizedNodeContainerSupport<AugmentationIdentifier, AugmentationNode> SUPPORT =
            new NormalizedNodeContainerSupport<>(AugmentationNode.class, ImmutableAugmentationNodeBuilder::create,
                    ImmutableAugmentationNodeBuilder::create);

    private final AugmentationNode emptyNode;

    AugmentationModificationStrategy(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        super(SUPPORT, EffectiveAugmentationSchema.create(schema, resolved), treeConfig);
        emptyNode = Builders.augmentationBuilder()
                .withNodeIdentifier(DataSchemaContextNode.augmentationIdentifierFrom(schema))
                .build();
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        AutomaticLifecycleMixin.checkApplicable(super::checkApplicable, emptyNode, path, modification, current,
            version);
    }
}
