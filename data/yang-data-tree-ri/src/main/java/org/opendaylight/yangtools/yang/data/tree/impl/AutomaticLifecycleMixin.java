/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

/**
 * Mixin-type support class for subclasses of {@link ModificationApplyOperation} which need to provide automatic
 * lifecycle management.
 */
final class AutomaticLifecycleMixin {
    /**
     * This is a capture of {@link ModificationApplyOperation#apply(ModifiedNode, TreeNode, Version)}.
     */
    @FunctionalInterface
    interface Apply {
        @Nullable TreeNode apply(ModifiedNode modification, @Nullable TreeNode currentMeta, Version version);
    }

    /**
     * This is a capture of
     * {@link SchemaAwareApplyOperation#applyWrite(ModifiedNode, NormalizedNode, TreeNode, Version)}.
     */
    @FunctionalInterface
    interface ApplyWrite {
        TreeNode applyWrite(ModifiedNode modification, NormalizedNode newValue, @Nullable TreeNode currentMeta,
            Version version);
    }

    private AutomaticLifecycleMixin() {
        // Hidden on purpose
    }

    static @Nullable TreeNode apply(final Apply delegate, final ApplyWrite writeDelegate,
            final NormalizedNode emptyNode, final ModifiedNode modification, final @Nullable TreeNode currentMeta,
            final Version version) {
        final @Nullable TreeNode ret;
        if (modification.getOperation() == LogicalOperation.DELETE) {
            if (modification.isEmpty()) {
                return delegate.apply(modification, currentMeta, version);
            }
            // Delete with children, implies it really is an empty write
            ret = verifyNotNull(writeDelegate.applyWrite(modification, emptyNode, currentMeta, version));
        } else if (modification.getOperation() == LogicalOperation.TOUCH && currentMeta == null) {
            ret = applyTouch(delegate, emptyNode, modification, null, version);
        } else {
            // No special handling required here, run normal apply operation
            ret = delegate.apply(modification, currentMeta, version);
        }

        return ret != null ? disappearResult(modification, ret, currentMeta) : null;
    }

    private static @Nullable TreeNode applyTouch(final Apply delegate, final NormalizedNode emptyNode,
            final ModifiedNode modification, final @Nullable TreeNode currentMeta, final Version version) {
        // Container is not present, let's take care of the 'magically appear' part of our job
        final var ret = delegate.apply(modification, fakeMeta(emptyNode, version), version);

        // If the delegate indicated SUBTREE_MODIFIED, account for the fake and report APPEARED
        if (modification.getModificationType() == ModificationType.SUBTREE_MODIFIED) {
            modification.resolveModificationType(ModificationType.APPEARED);
        }
        return ret;
    }

    private static @Nullable TreeNode disappearResult(final ModifiedNode modification, final @NonNull TreeNode result,
            final @Nullable TreeNode currentMeta) {
        // Check if the result is in fact empty before pulling any tricks
        final var data = result.getData();
        if (!(data instanceof NormalizedNodeContainer<?> container)) {
            throw new IllegalStateException("Unhandled data " + data);
        }
        if (!container.isEmpty()) {
            return result;
        }

        // We are pulling the 'disappear' trick, but what we report can be three different things
        final ModificationType finalType;
        if (currentMeta == null) {
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
        return null;
    }

    private static @NonNull TreeNode fakeMeta(final NormalizedNode emptyNode, final Version version) {
        return TreeNode.of(emptyNode, version);
    }

    private static boolean isEmpty(final TreeNode treeNode) {
        final var data = treeNode.getData();
        if (data instanceof NormalizedNodeContainer<?> container) {
            return container.isEmpty();
        }
        throw new IllegalStateException("Unhandled data " + data);
    }
}
