/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Structural containers are special in that they appear when implied by child
 * nodes and disappear whenever they are empty. We could implement this as a
 * subclass of {@link SchemaAwareApplyOperation}, but the automatic semantic
 * is quite different from all the other strategies. We create a
 * {@link PresenceContainerModificationStrategy} to tap into that logic, but
 * wrap it so we only call out into it
 */
final class StructuralContainerModificationStrategy extends ModificationApplyOperation {
    private final PresenceContainerModificationStrategy delegate;

    StructuralContainerModificationStrategy(final ContainerSchemaNode schemaNode, TreeType treeType) {
        this.delegate = new PresenceContainerModificationStrategy(schemaNode, treeType);
    }

    private Optional<TreeNode> ensureCurrent(final Optional<TreeNode> current) {
        if (current.isPresent()) {
            return current;
        }

        /*
         * Structural containers are created as needed, so we pretend this container
         * is here.
         */
        final ContainerNode container = ImmutableNodes.containerNode(delegate.getSchema().getQName());
        return Optional.of(TreeNodeFactory.createTreeNode(container, Version.initial()));
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta, final Version version) {
        final Optional<TreeNode> ret = delegate.apply(modification, ensureCurrent(storeMeta), version);
        if (ret.isPresent()) {
            /*
             * Check if there are any child nodes left. If there are none, make sure
             * we remove this node.
             */
            final NormalizedNodeContainer<?, ?, ?> data = (NormalizedNodeContainer<?, ?, ?>) ret.get().getData();
            if (data.getValue().isEmpty()) {
                if (storeMeta.isPresent()) {
                    modification.resolveModificationType(ModificationType.DELETE);
                } else {
                    modification.resolveModificationType(ModificationType.UNMODIFIED);
                }
                return Optional.absent();
            }
        }

        return ret;
    }

    @Override
    void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        delegate.checkApplicable(path, modification, ensureCurrent(current));
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) throws IllegalArgumentException {
        delegate.verifyStructure(modification, verifyChildren);
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }
}
