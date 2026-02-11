/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNodeAccess;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

/**
 * Mixin-type support class for subclasses of {@link ModificationApplyOperation} which need to provide automatic
 * lifecycle management.
 */
@NonNullByDefault
sealed interface AutomaticLifecycleMixin extends TreeNodeAccess
        permits ChoiceModificationStrategy, ContainerModificationStrategy.Structural, ListModificationStrategy,
                MapModificationStrategy {
    /**
     * Invoke {@link ModificationApplyOperation#apply(ModifiedNode, TreeNode, Version)} on superclass.
     */
    @Nullable TreeNode superApply(ModifiedNode modification, @Nullable TreeNode currentMeta, Version version);

    /**
     * Invoke {@link SchemaAwareApplyOperation#applyWrite(ModifiedNode, NormalizedNode, TreeNode, Version).
     */
    TreeNode thisApplyWrite(ModifiedNode modification, NormalizedNode newValue, @Nullable TreeNode currentMeta,
        Version version);

    /**
     * Implementation of {@link SchemaAwareApplyOperation#applyWrite(ModifiedNode, NormalizedNode, TreeNode, Version)}
     * users should delegate to.
     */
    default @Nullable TreeNode apply(final NormalizedNode emptyNode, final ModifiedNode modification,
            final @Nullable TreeNode currentMeta, final Version version) {
        final @Nullable TreeNode ret;
        if (modification.getOperation() == LogicalOperation.DELETE) {
            if (modification.isEmpty()) {
                return superApply(modification, currentMeta, version);
            }
            // Delete with children, implies it really is an empty write
            ret = verifyNotNull(thisApplyWrite(modification, emptyNode, currentMeta, version));
        } else if (modification.getOperation() == LogicalOperation.TOUCH && currentMeta == null) {
            ret = applyTouch(this, emptyNode, modification, null, version);
        } else {
            // No special handling required here, run normal apply operation
            ret = superApply(modification, currentMeta, version);
        }

        return ret == null ? null : disappearResult(modification, ret, currentMeta);
    }

    private static @Nullable TreeNode applyTouch(final AutomaticLifecycleMixin self, final NormalizedNode emptyNode,
            final ModifiedNode modification, final @Nullable TreeNode currentMeta, final Version version) {
        // Container is not present, let's take care of the 'magically appear' part of our job
        final var ret = self.superApply(modification, self.newTreeNode(emptyNode, version), version);

        // If the delegate indicated SUBTREE_MODIFIED, account for the fake and report APPEARED
        if (modification.getModificationType() == ModificationType.SUBTREE_MODIFIED) {
            modification.resolveModificationType(ModificationType.APPEARED);
        }
        return ret;
    }

    private static @Nullable TreeNode disappearResult(final ModifiedNode modification, final TreeNode result,
            final @Nullable TreeNode currentMeta) {
        // Check if the result is in fact empty before pulling any tricks
        final var data = result.data();
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
}
