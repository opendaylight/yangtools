/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

abstract class AbstractModifiedNodeBasedCandidateNode implements DataTreeCandidateNode {

    private static final Function<NormalizedNode<?, ?>, DataTreeCandidateNode> TO_UNMODIFIED_NODES = new Function<NormalizedNode<?, ?>, DataTreeCandidateNode>() {
        @Override
        public DataTreeCandidateNode apply(final NormalizedNode<?, ?> input) {
            return AbstractRecursiveCandidateNode.unmodifiedNode(input);
        }
    };

    private final ModifiedNode mod;
    private final TreeNode newMeta;
    private final TreeNode oldMeta;

    protected AbstractModifiedNodeBasedCandidateNode(final ModifiedNode mod,
            final TreeNode oldMeta, final TreeNode newMeta) {
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

    private static final TreeNode childMeta(final TreeNode parent, final PathArgument id) {
        if (parent != null) {
            return parent.getChild(id).orNull();
        } else {
            return null;
        }
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
    private static NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> getContainer(@Nullable final TreeNode meta) {
        return (meta == null ? null : (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>)meta.getData());
    }

    private ChildNode childNode(final ModifiedNode childMod) {
        final PathArgument id = childMod.getIdentifier();
        return new ChildNode(childMod, childMeta(oldMeta, id), childMeta(newMeta, id));
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        switch (mod.getModificationType()) {
        case SUBTREE_MODIFIED:
            return Collections2.transform(mod.getChildren(), new Function<ModifiedNode, DataTreeCandidateNode>() {
                @Override
                public DataTreeCandidateNode apply(final ModifiedNode input) {
                    return childNode(input);
                }
            });
        case UNMODIFIED:
            // Unmodified node, but we still need to resolve potential children. canHaveChildren returns
            // false if both arguments are null.
            if (canHaveChildren(oldMeta, newMeta)) {
                return Collections2.transform(getContainer(newMeta != null ? newMeta : oldMeta).getValue(), TO_UNMODIFIED_NODES);
            } else {
                return Collections.emptyList();
            }
        case DELETE:
        case WRITE:
            // This is unusual, the user is requesting we follow into an otherwise-terminal node.
            // We need to fudge things based on before/after data to correctly fake the expectations.
            if (canHaveChildren(oldMeta, newMeta)) {
                return AbstractDataTreeCandidateNode.deltaChildren(getContainer(oldMeta), getContainer(newMeta)).values();
            } else {
                return Collections.emptyList();
            }
        default:
            throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
        }
    }

    @Override
    public ModificationType getModificationType() {
        return Verify.verifyNotNull(mod.getModificationType(), "Node %s does not have resolved modification type", mod);
    }

    private Optional<NormalizedNode<?, ?>> optionalData(final TreeNode meta) {
        if (meta != null) {
            return Optional.<NormalizedNode<?,?>>of(meta.getData());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return optionalData(newMeta);
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return optionalData(oldMeta);
    }

    @Override
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        switch (mod.getModificationType()) {
        case SUBTREE_MODIFIED:
            final Optional<ModifiedNode> childMod = mod.getChild(identifier);
            if (childMod.isPresent()) {
                return childNode(childMod.get());
            }
            return null;
        case DELETE:
        case UNMODIFIED:
        case WRITE:
            // FIXME: this is a linear walk. We need a Map of these in order to
            //        do something like getChildMap().get(identifier);
            for (DataTreeCandidateNode c : getChildNodes()) {
                if (identifier.equals(c.getIdentifier())) {
                    return c;
                }
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
        public PathArgument getIdentifier() {
            return getMod().getIdentifier();
        }
    }
}