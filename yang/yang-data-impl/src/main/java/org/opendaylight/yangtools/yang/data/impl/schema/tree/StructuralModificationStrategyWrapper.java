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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle managing modification strategy wrapper. Creates and deletes node container as needed.
 * Does not preserve empty ndoe containers.
 */
final class StructuralModificationStrategyWrapper extends ModificationApplyOperation {

    private static final Logger LOG = LoggerFactory.getLogger(StructuralModificationStrategyWrapper.class);

    private static final Version FAKE_VERSION = Version.initial();
    private final ModificationApplyOperation delegate;
    private final NormalizedNode<?, ?> emptyNode;

    StructuralModificationStrategyWrapper(final ModificationApplyOperation delegate, final NormalizedNode<?, ?> emptyNode) {
        this.delegate = delegate;
        this.emptyNode = emptyNode;
    }

    private Optional<TreeNode> fakeMeta(final Version version) {
        return Optional.of(TreeNodeFactory.createTreeNode(emptyNode, version));
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta, final Version version) {
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
        } else if (modification.getModificationType() == ModificationType.DISAPPEARED) {
            return Optional.absent();
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
            // Structural containers are created as needed, so we pretend this container is here
            delegate.checkApplicable(path, modification, fakeMeta(FAKE_VERSION), version);
        } else if (current.isPresent()) {
            delegate.checkApplicable(path, modification, current, version);
        } else if(modification instanceof ModifiedNode) {
            // In case of structural parent container, current is always non-present (because parent's current node is fake
            // and cannot provide current nodes for its children, even if they exist). And since "current" argument here is
            // retrieved from parent's current node, it's important to verify current tree node if current.isPresent == false
            // It might appear indicating modification under a structural parent. Real current node value is important here
            // (just) to decide whether to proceed with validation or not. Because if current is really non-present, it means
            // that this node was deleted
            final Optional<TreeNode> currentReal = apply(((ModifiedNode) modification), current, version);
            if(currentReal.isPresent()) {
                delegate.checkApplicable(path, modification, current, version);
            }
        }

        LOG.debug("Could not validate {}, does not implement expected class {}", modification, ModifiedNode.class);
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) throws IllegalArgumentException {
        delegate.verifyStructure(modification, verifyChildren);
    }

    @Override
    void recursivelyVerifyStructure(NormalizedNode<?, ?> value) {
        delegate.recursivelyVerifyStructure(value);
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    void mergeIntoModifiedNode(ModifiedNode modification, NormalizedNode<?, ?> value, Version version) {
        delegate.mergeIntoModifiedNode(modification, value, version);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

}
