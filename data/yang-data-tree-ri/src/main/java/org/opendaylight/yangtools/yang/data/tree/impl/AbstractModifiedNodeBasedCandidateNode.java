/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidateNodes;

abstract class AbstractModifiedNodeBasedCandidateNode implements DataTreeCandidateNode {
    private final ModifiedNode mod;
    private final TreeNode newMeta;
    private final TreeNode oldMeta;

    protected AbstractModifiedNodeBasedCandidateNode(final ModifiedNode mod, final TreeNode oldMeta,
            final TreeNode newMeta) {
        this.newMeta = newMeta;
        this.oldMeta = oldMeta;
        this.mod = requireNonNull(mod);
    }

    protected final ModifiedNode getMod() {
        return mod;
    }

    protected final TreeNode getNewMeta() {
        return newMeta;
    }

    protected final TreeNode getOldMeta() {
        return oldMeta;
    }

    private static TreeNode childMeta(final TreeNode parent, final PathArgument id) {
        return parent == null ? null : parent.childByArg(id);
    }

    private static boolean canHaveChildren(final @Nullable TreeNode oldMeta, final @Nullable TreeNode newMeta) {
        if (oldMeta != null) {
            return oldMeta.getData() instanceof NormalizedNodeContainer;
        }
        if (newMeta != null) {
            return newMeta.getData() instanceof NormalizedNodeContainer;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static DistinctNodeContainer<PathArgument, NormalizedNode> getContainer(
            final @Nullable TreeNode meta) {
        return meta == null ? null : (DistinctNodeContainer<PathArgument, NormalizedNode>)meta.getData();
    }

    private @NonNull ChildNode childNode(final ModifiedNode childMod) {
        final var id = childMod.getIdentifier();
        return new ChildNode(childMod, childMeta(oldMeta, id), childMeta(newMeta, id));
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        return switch (mod.getModificationType()) {
            case APPEARED, DISAPPEARED, SUBTREE_MODIFIED -> Collections2.transform(mod.getChildren(), this::childNode);
            case UNMODIFIED -> {
                // Unmodified node, but we still need to resolve potential children. canHaveChildren returns
                // false if both arguments are null.
                if (!canHaveChildren(oldMeta, newMeta)) {
                    yield ImmutableList.of();
                }
                yield Collections2.transform(getContainer(newMeta != null ? newMeta : oldMeta).body(),
                    DataTreeCandidateNodes::unmodified);
            }
            case DELETE, WRITE -> {
                // This is unusual, the user is requesting we follow into an otherwise-terminal node.
                // We need to fudge things based on before/after data to correctly fake the expectations.
                if (!canHaveChildren(oldMeta, newMeta)) {
                    yield ImmutableList.of();
                }
                yield DataTreeCandidateNodes.containerDelta(getContainer(oldMeta), getContainer(newMeta));
            }
        };
    }

    @Override
    public ModificationType modificationType() {
        return verifyNotNull(mod.getModificationType(), "Node %s does not have resolved modification type", mod);
    }

    @Override
    public final NormalizedNode dataBefore() {
        return data(oldMeta);
    }

    @Override
    public final NormalizedNode dataAfter() {
        return data(newMeta);
    }

    private static @Nullable NormalizedNode data(final TreeNode meta) {
        return meta == null ? null : meta.getData();
    }


    @Override
    public final DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        final var identifier = requireNonNull(childName);
        return switch (mod.getModificationType()) {
            case APPEARED, DISAPPEARED, SUBTREE_MODIFIED -> {
                final var child = mod.childByArg(identifier);
                yield child == null ? null : childNode(child);
            }
            case UNMODIFIED -> {
                if (!canHaveChildren(oldMeta, newMeta)) {
                    yield null;
                }
                final var child = getContainer(newMeta != null ? newMeta : oldMeta).childByArg(identifier);
                yield child == null ? null : DataTreeCandidateNodes.unmodified(child);
            }
            case DELETE, WRITE -> {
                if (!canHaveChildren(oldMeta, newMeta)) {
                    yield null;
                }
                yield DataTreeCandidateNodes.containerDelta(getContainer(oldMeta), getContainer(newMeta), identifier);
            }
        };
    }

    private static final class ChildNode extends AbstractModifiedNodeBasedCandidateNode {
        ChildNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument name() {
            return getMod().getIdentifier();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{mod = " + mod + ", oldMeta = " + oldMeta + ", newMeta = " + newMeta + "}";
    }
}
