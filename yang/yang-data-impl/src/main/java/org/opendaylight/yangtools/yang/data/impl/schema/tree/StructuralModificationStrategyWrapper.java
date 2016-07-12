/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * Lifecycle managing modification strategy wrapper. Creates and deletes node containers as needed.
 * Does not preserve empty node containers.
 *
 * Structural node containers are special in that they appear when implied by child nodes and disappear whenever they are
 * empty. We could implement this as a subclass of {@link SchemaAwareApplyOperation}, but the automatic semantic
 * is quite different from all the other strategies. We receive to tap into that
 * logic, but wrap it so we only call out into it.
 *
 * For containers we do not use {@link PresenceContainerModificationStrategy} because
 * it enforces presence of mandatory leaves, which is not something we want here, as structural containers are not
 * root anchors for that validation.
 */
final class StructuralModificationStrategyWrapper extends ModificationApplyOperation {

    /**
     * Fake TreeNode version used in
     * {@link #checkApplicable(YangInstanceIdentifier, NodeModification, Optional, Version)}.
     * It is okay to use a global constant, as the delegate will ignore it anyway. For
     * {@link #apply(ModifiedNode, Optional, Version)} we will use the appropriate version as provided to us.
     */
    private static final Version FAKE_VERSION = Version.initial();
    private final ModificationApplyOperation delegate;
    private final NormalizedNode<?, ?> emptyNode;

    StructuralModificationStrategyWrapper(final ModificationApplyOperation delegate, final NormalizedNode<?, ?> emptyNode) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.emptyNode = emptyNode;
    }

    private Optional<TreeNode> fakeMeta(final Version version) {
        return Optional.of(TreeNodeFactory.createTreeNode(emptyNode, version));
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta, final Version version) {
        // Apply can be called multiple times and if DISAPPEARED was already set for current node, absent
        // can be safely returned right away
        if (modification.getModificationType() == ModificationType.DISAPPEARED) {
            return Optional.absent();
        }

        final Optional<TreeNode> ret;
        if (modification.getOperation() == LogicalOperation.TOUCH && !storeMeta.isPresent()) {
            // Node container is not present, let's take care of the 'magically appear' part of our job
            ret = delegate.apply(modification, fakeMeta(version), version);

            // Fake node container got removed: that is a no-op
            if (!ret.isPresent()) {
                modification.resolveModificationType(ModificationType.UNMODIFIED);
                return ret;
            }

            // If the delegate indicated SUBTREE_MODIFIED, account for the fake and report APPEARED
            if (modification.getModificationType() == ModificationType.SUBTREE_MODIFIED) {
                modification.resolveModificationType(ModificationType.APPEARED);
            }
        } else {
            // Node container is present, run normal apply operation
            ret = delegate.apply(modification, storeMeta, version);

            // Node container was explicitly deleted, no magic required
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
            // Structural containers are created as needed, so we pretend this
            // container is here
            delegate.checkApplicable(path, modification, fakeMeta(FAKE_VERSION), version);
        } else {
            if (modification instanceof ModifiedNode
                    && ((ModifiedNode) modification).getModificationType() == ModificationType.DISAPPEARED) {
                // Skip delegate validation if disappearing
                return;
            }

            delegate.checkApplicable(path, modification, current, version);
        }
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) throws IllegalArgumentException {
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
    void mergeIntoModifiedNode(final ModifiedNode modification, final NormalizedNode<?, ?> value, final Version version) {
        delegate.mergeIntoModifiedNode(modification, value, version);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

}
