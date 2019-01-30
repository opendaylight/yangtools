/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

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
final class AutomaticLifecycleMixin extends NonApplyDelegatedModificationApplyOperation {
    /**
     * Fake TreeNode version used in
     * {@link #checkApplicable(ModificationPath, NodeModification, Optional, Version)}.
     * It is okay to use a global constant, as the delegate will ignore it anyway. For
     * {@link #apply(ModifiedNode, Optional, Version)} we will use the appropriate version as provided to us.
     */
    private static final Version FAKE_VERSION = Version.initial();

    private final SchemaAwareApplyOperation delegate;
    private final NormalizedNode<?, ?> emptyNode;

    AutomaticLifecycleMixin(final SchemaAwareApplyOperation delegate, final NormalizedNode<?, ?> emptyNode) {
        this.delegate = requireNonNull(delegate);
        this.emptyNode = requireNonNull(emptyNode);
    }

    @Override
    SchemaAwareApplyOperation delegate() {
        return delegate;
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        // The only way a tree node can disappear is through delete (which we handle here explicitly) or through
        // actions of disappearResult(). It is therefore safe to perform Optional.get() on the results of
        // delegate.apply()
        final TreeNode ret;
        if (modification.getOperation() == LogicalOperation.DELETE) {
            if (modification.getChildren().isEmpty()) {
                return delegate.apply(modification, storeMeta, version);
            }
            // Delete with children, implies it really is an empty write
            ret = delegate.applyWrite(modification, emptyNode, storeMeta, version);
        } else if (modification.getOperation() == LogicalOperation.TOUCH && !storeMeta.isPresent()) {
            ret = applyTouch(modification, storeMeta, version);
        } else {
            // No special handling required here, run normal apply operation
            ret = delegate.apply(modification, storeMeta, version).get();
        }

        return disappearResult(modification, ret, storeMeta);
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        if (modification.getOperation() == LogicalOperation.TOUCH && !current.isPresent()) {
            // Structural containers are created as needed, so we pretend this container is here
            delegate.checkApplicable(path, modification, fakeMeta(emptyNode, FAKE_VERSION), version);
        } else {
            delegate.checkApplicable(path, modification, current, version);
        }
    }

    private TreeNode applyTouch(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        // Container is not present, let's take care of the 'magically appear' part of our job
        final Optional<TreeNode> ret = delegate.apply(modification, fakeMeta(emptyNode, version), version);

        // If the delegate indicated SUBTREE_MODIFIED, account for the fake and report APPEARED
        if (modification.getModificationType() == ModificationType.SUBTREE_MODIFIED) {
            modification.resolveModificationType(ModificationType.APPEARED);
        }
        return ret.get();
    }

    private static Optional<TreeNode> disappearResult(final ModifiedNode modification, final TreeNode result,
            final Optional<TreeNode> storeMeta) {
        // Check if the result is in fact empty before pulling any tricks
        if (!isEmpty(result)) {
            return Optional.of(result);
        }

        // We are pulling the 'disappear' trick, but what we report can be three different things
        final ModificationType finalType;
        if (!storeMeta.isPresent()) {
            // ... there was nothing in the datastore, no change
            finalType = ModificationType.UNMODIFIED;
        } else if (modification.getModificationType() == ModificationType.WRITE) {
            // ... this was an empty write, possibly originally a delete
            finalType = ModificationType.DELETE;
        } else {
            // ... it really disappeared
            finalType = ModificationType.DISAPPEARED;
        }
        modification.resolveModificationType(finalType);
        return Optional.empty();
    }

    private static Optional<TreeNode> fakeMeta(final NormalizedNode<?, ?> emptyNode, final Version version) {
        return Optional.of(TreeNodeFactory.createTreeNode(emptyNode, version));
    }

    private static boolean isEmpty(final TreeNode treeNode) {
        final NormalizedNode<?, ?> data = treeNode.getData();
        if (data instanceof NormalizedNodeContainer) {
            return ((NormalizedNodeContainer<?, ?, ?>) data).getValue().isEmpty();
        }
        if (data instanceof OrderedNodeContainer) {
            return ((OrderedNodeContainer<?>) data).getSize() == 0;
        }
        throw new IllegalStateException("Unhandled data " + data);
    }
}
