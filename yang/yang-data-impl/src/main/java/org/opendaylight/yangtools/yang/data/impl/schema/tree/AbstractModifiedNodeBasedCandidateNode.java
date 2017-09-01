/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

abstract class AbstractModifiedNodeBasedCandidateNode implements DataTreeCandidateNode {
    private final ModifiedNode mod;
    private final TreeNode newMeta;
    private final TreeNode oldMeta;

    protected AbstractModifiedNodeBasedCandidateNode(final ModifiedNode mod, final TreeNode oldMeta,
            final TreeNode newMeta) {
        this.newMeta = newMeta;
        this.oldMeta = oldMeta;
        this.mod = Preconditions.checkNotNull(mod);
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
        return parent == null ? null : parent.getChild(id).orNull();
    }

    private static boolean canHaveChildren(@Nullable final TreeNode oldMeta, @Nullable final TreeNode newMeta) {
        if (oldMeta != null) {
            return oldMeta.getData() instanceof NormalizedNodeContainer;
        }
        if (newMeta != null) {
            return newMeta.getData() instanceof NormalizedNodeContainer;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> getContainer(
            @Nullable final TreeNode meta) {
        return meta == null ? null : (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>)meta.getData();
    }

    private ChildNode childNode(final ModifiedNode childMod) {
        final PathArgument id = childMod.getIdentifier();
        return new ChildNode(childMod, childMeta(oldMeta, id), childMeta(newMeta, id));
    }

    @Override
    @Nonnull
    public Collection<DataTreeCandidateNode> getChildNodes() {
        switch (mod.getModificationType()) {
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
                return Collections2.transform(mod.getChildren(), this::childNode);
            case UNMODIFIED:
                // Unmodified node, but we still need to resolve potential children. canHaveChildren returns
                // false if both arguments are null.
                if (!canHaveChildren(oldMeta, newMeta)) {
                    return ImmutableList.of();
                }

                return Collections2.transform(getContainer(newMeta != null ? newMeta : oldMeta).getValue(),
                    AbstractRecursiveCandidateNode::unmodifiedNode);
            case DELETE:
            case WRITE:
                // This is unusual, the user is requesting we follow into an otherwise-terminal node.
                // We need to fudge things based on before/after data to correctly fake the expectations.
                if (!canHaveChildren(oldMeta, newMeta)) {
                    return ImmutableList.of();
                }
                return AbstractDataTreeCandidateNode.deltaChildren(getContainer(oldMeta), getContainer(newMeta));
            default:
                throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
        }
    }

    @Override
    @Nonnull
    public ModificationType getModificationType() {
        return Verify.verifyNotNull(mod.getModificationType(), "Node %s does not have resolved modification type", mod);
    }

    private static Optional<NormalizedNode<?, ?>> optionalData(final TreeNode meta) {
        return meta == null ? Optional.absent() : Optional.of(meta.getData());
    }

    @Override
    @Nonnull
    public final Optional<NormalizedNode<?, ?>> getDataAfter() {
        return optionalData(newMeta);
    }

    @Override
    @Nonnull
    public final Optional<NormalizedNode<?, ?>> getDataBefore() {
        return optionalData(oldMeta);
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        switch (mod.getModificationType()) {
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
                final Optional<ModifiedNode> childMod = mod.getChild(identifier);
                if (childMod.isPresent()) {
                    return childNode(childMod.get());
                }
                return null;
            case UNMODIFIED:
                if (!canHaveChildren(oldMeta, newMeta)) {
                    return null;
                }
                final Optional<NormalizedNode<?, ?>> maybeChild = getContainer(newMeta != null ? newMeta : oldMeta)
                        .getChild(identifier);
                return maybeChild.isPresent() ? AbstractRecursiveCandidateNode.unmodifiedNode(maybeChild.get()) : null;
            case DELETE:
            case WRITE:
                if (!canHaveChildren(oldMeta, newMeta)) {
                    return null;
                }
                return AbstractDataTreeCandidateNode.deltaChild(getContainer(oldMeta), getContainer(newMeta),
                    identifier);
            default:
                throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
        }
    }

    private static final class ChildNode extends AbstractModifiedNodeBasedCandidateNode {
        ChildNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        @Nonnull
        public PathArgument getIdentifier() {
            return getMod().getIdentifier();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{mod = " + this.mod + ", oldMeta = " + this.oldMeta + ", newMeta = "
                + this.newMeta + "}";
    }
}
