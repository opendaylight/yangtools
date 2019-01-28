/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * Mixin-type support class for subclasses of {@link ModificationApplyOperation} which need to provide automatic
 * lifecycle management.
 */
final class AutomaticLifecycleMixin {
    /**
     * This is a capture of {@link ModificationApplyOperation#apply(ModifiedNode, Optional, Version)}.
     */
    @FunctionalInterface
    interface Apply {
        Optional<TreeNode> apply(ModifiedNode modification, Optional<TreeNode> storeMeta, Version version);
    }

    /**
     * This is a capture of
     * {@link ModificationApplyOperation#checkApplicable(ModificationPath, NodeModification, Optional, Version)}.
     */
    @FunctionalInterface
    interface CheckApplicable {
        void checkApplicable(ModificationPath path, NodeModification modification, Optional<TreeNode> current,
                Version version) throws DataValidationFailedException;
    }

    /**
     * Fake TreeNode version used in
     * {@link #checkApplicable(ModificationPath, NodeModification, Optional, Version)}.
     * It is okay to use a global constant, as the delegate will ignore it anyway. For
     * {@link #apply(ModifiedNode, Optional, Version)} we will use the appropriate version as provided to us.
     */
    private static final Version FAKE_VERSION = Version.initial();

    private AutomaticLifecycleMixin() {

    }

    static Optional<TreeNode> apply(final Apply delegate, final NormalizedNode<?, ?> emptyNode,
            final ModifiedNode modification, final Optional<TreeNode> storeMeta, final Version version) {
        final Optional<TreeNode> ret;
        if (modification.getOperation() == LogicalOperation.TOUCH && !storeMeta.isPresent()) {
            // Container is not present, let's take care of the 'magically appear' part of our job
            ret = delegate.apply(modification, fakeMeta(emptyNode, version), version);

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
         * our job. Check if there are any child nodes left.
         */
        final NormalizedNode<?, ?> data = ret.get().getData();
        final boolean empty;
        if (data instanceof NormalizedNodeContainer) {
            empty = ((NormalizedNodeContainer<?, ?, ?>) data).getValue().isEmpty();
        } else if (data instanceof OrderedNodeContainer) {
            empty = ((OrderedNodeContainer<?>) data).getSize() == 0;
        } else {
            throw new IllegalStateException("Unhandled data " + data);
        }
        if (!empty) {
            // We have some nodes, bail out
            return ret;
        }

        // We are pulling the 'disappear' trick, but what we report can be three different things
        final ModificationType finalType;
        if (!storeMeta.isPresent()) {
            // ... there was nothing in the datastore, no change
            finalType = ModificationType.UNMODIFIED;
        } else if (modification.getOperation() == LogicalOperation.WRITE) {
            // ... this was an empty write, possibily originally a delete
            finalType = ModificationType.DELETE;
        } else {
            // ... it really disappeared
            finalType = ModificationType.DISAPPEARED;
        }
        modification.resolveModificationType(finalType);
        return Optional.empty();
    }

    static void checkApplicable(final CheckApplicable delegate, final NormalizedNode<?, ?> emptyNode,
            final ModificationPath path, final NodeModification modification, final Optional<TreeNode> current,
            final Version version) throws DataValidationFailedException {
        if (modification.getOperation() == LogicalOperation.TOUCH && !current.isPresent()) {
            // Structural containers are created as needed, so we pretend this container is here
            delegate.checkApplicable(path, modification, fakeMeta(emptyNode, FAKE_VERSION), version);
        } else {
            delegate.checkApplicable(path, modification, current, version);
        }
    }

    private static Optional<TreeNode> fakeMeta(final NormalizedNode<?, ?> emptyNode, final Version version) {
        return Optional.of(TreeNodeFactory.createTreeNode(emptyNode, version));
    }
}
