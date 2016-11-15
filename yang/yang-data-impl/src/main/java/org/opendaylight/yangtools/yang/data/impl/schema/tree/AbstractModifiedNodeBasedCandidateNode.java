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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractModifiedNodeBasedCandidateNode implements DataTreeCandidateNode {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractModifiedNodeBasedCandidateNode.class);

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
        if (parent != null) {
            return parent.getChild(id).orNull();
        }
        return null;
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
        final TreeNode oldChild = childMeta(oldMeta, id);
        final TreeNode newChild = childMeta(newMeta, id);

        switch (childMod.getModificationType()) {
            case DELETE:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
                if (oldChild == null) {
                    LOG.warn("Encountered child without a corresponsding before-image, dumping relevant state",
                        new Throwable());
                    LOG.warn("Parent modification {}", this);
                    LOG.warn("Child node {}", childMod);
                    LOG.warn("Child old meta {}", oldChild);
                    LOG.warn("Child new meta {}", newChild);
                }
                break;
            default:
                break;
        }
        return new ChildNode(childMod, oldChild, newChild);
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
            if (canHaveChildren(oldMeta, newMeta)) {
                return Collections2.transform(getContainer(newMeta != null ? newMeta : oldMeta).getValue(),
                    AbstractRecursiveCandidateNode::unmodifiedNode);
            }
            return ImmutableList.of();
        case DELETE:
        case WRITE:
            // This is unusual, the user is requesting we follow into an otherwise-terminal node.
            // We need to fudge things based on before/after data to correctly fake the expectations.
            if (canHaveChildren(oldMeta, newMeta)) {
                return AbstractDataTreeCandidateNode.deltaChildren(getContainer(oldMeta), getContainer(newMeta));
            }
            return ImmutableList.of();
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
        if (meta != null) {
            return Optional.of(meta.getData());
        }
        return Optional.absent();
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
            if (canHaveChildren(oldMeta, newMeta)) {
                final Optional<NormalizedNode<?, ?>> maybeChild = getContainer(newMeta != null ? newMeta : oldMeta)
                        .getChild(identifier);
                if (maybeChild.isPresent()) {
                    return AbstractRecursiveCandidateNode.unmodifiedNode(maybeChild.get());
                }
            }
            return null;
        case DELETE:
        case WRITE:
            if (canHaveChildren(oldMeta, newMeta)) {
                return AbstractDataTreeCandidateNode.deltaChild(getContainer(oldMeta), getContainer(newMeta),
                    identifier);
            }
            return null;
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
        return this.getClass().getSimpleName() + "{mod = " + this.mod + ", oldMeta = " + this.oldMeta + ", newMeta = " +
                this.newMeta + "}";
    }
}
