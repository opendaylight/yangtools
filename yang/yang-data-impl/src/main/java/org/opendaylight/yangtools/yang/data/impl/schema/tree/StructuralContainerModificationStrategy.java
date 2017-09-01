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
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
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
final class StructuralContainerModificationStrategy extends ModificationApplyOperation {
    /**
     * Fake TreeNode version used in
     * {@link #checkApplicable(YangInstanceIdentifier, NodeModification, Optional, Version)}.
     * It is okay to use a global constant, as the delegate will ignore it anyway. For
     * {@link #apply(ModifiedNode, Optional, Version)} we will use the appropriate version as provided to us.
     */
    private static final Version FAKE_VERSION = Version.initial();
    private final ContainerModificationStrategy delegate;

    StructuralContainerModificationStrategy(final ContainerSchemaNode schemaNode,
        final DataTreeConfiguration treeConfig) {
        this.delegate = new ContainerModificationStrategy(schemaNode, treeConfig);
    }

    private Optional<TreeNode> fakeMeta(final Version version) {
        final ContainerNode container = ImmutableNodes.containerNode(delegate.getSchema().getQName());
        return Optional.of(TreeNodeFactory.createTreeNode(container, version));
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        final Optional<TreeNode> ret;
        if (modification.getOperation() == LogicalOperation.TOUCH && !storeMeta.isPresent()) {
            // Container is not present, let's take care of the 'magically appear' part of our job
            ret = delegate.apply(modification, fakeMeta(version), version);

            // Fake container got removed: that is a no-op
            if (!ret.isPresent()) {
                modification.resolveModificationType(ModificationType.UNMODIFIED);
                return ret;
            }

            // If the delegate indicated SUBTREE_MODIFIED, account for the fake and report APPEARED
            if (modification.getModificationType() == ModificationType.SUBTREE_MODIFIED) {
                modification.resolveModificationType(ModificationType.APPEARED);
            }
        } else {
            // Container is present, run normal apply operation
            ret = delegate.apply(modification, storeMeta, version);

            // Container was explicitly deleted, no magic required
            if (!ret.isPresent()) {
                return ret;
            }
        }

        /*
         * At this point ret is guaranteed to be present. We need to take care of the 'magically disappear' part of
         * our job. Check if there are any child nodes left. If there are none, remove this container and turn the
         * modification into a DISAPPEARED.
         */
        if (((NormalizedNodeContainer<?, ?, ?>) ret.get().getData()).getValue().isEmpty()) {
            modification.resolveModificationType(ModificationType.DISAPPEARED);
            return Optional.absent();
        }

        return ret;
    }

    @Override
    void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        if (modification.getOperation() == LogicalOperation.TOUCH && !current.isPresent()) {
            // Structural containers are created as needed, so we pretend this container is here
            delegate.checkApplicable(path, modification, fakeMeta(FAKE_VERSION), version);
        } else {
            delegate.checkApplicable(path, modification, current, version);
        }
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        delegate.verifyStructure(modification, verifyChildren);
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        delegate.recursivelyVerifyStructure(value);
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode modification, final NormalizedNode<?, ?> value,
            final Version version) {
        delegate.mergeIntoModifiedNode(modification, value, version);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }
}
