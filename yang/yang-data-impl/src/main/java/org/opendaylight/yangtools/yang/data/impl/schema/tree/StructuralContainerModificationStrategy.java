/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Structural containers are special in that they appear when implied by child nodes and disappear whenever they are
 * empty. We could implement this as a subclass of {@link SchemaAwareApplyOperation}, but the automatic semantic
 * is quite different from all the other strategies. We create a {@link ContainerModificationStrategy} to tap into that
 * logic, but wrap it so we only call out into it. We do not use {@link PresenceContainerModificationStrategy} because
 * it enforces presence of mandatory leaves, which is not something we want here, as structural containers are not
 * root anchors for that validation.
 */
final class StructuralContainerModificationStrategy extends ContainerModificationStrategy {
    private final ContainerNode emptyNode;

    StructuralContainerModificationStrategy(final ContainerSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(schema, treeConfig);
        this.emptyNode = ImmutableNodes.containerNode(schema.getQName());
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    protected void checkTouchApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        checkTouchApplicable(path, modification, current, version, emptyNode);
    }
}
