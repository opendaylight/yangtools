/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Augmentation;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationModificationStrategy
        extends DataNodeContainerModificationStrategy<AugmentationSchemaNode> {
    private static final Augmentation SUPPORT = new Augmentation();

    AugmentationModificationStrategy(final AugmentationSchemaNode schema, final DataNodeContainer resolved,
            final DataTreeConfiguration treeConfig) {
        super(SUPPORT, EffectiveAugmentationSchema.create(schema, resolved), treeConfig);
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode(), modification, storeMeta,
            version);
    }
}
