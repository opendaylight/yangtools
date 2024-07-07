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

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.spi.AbstractDataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidateNodes;
import org.opendaylight.yangtools.yang.data.tree.spi.ImmutableCandidateCreated;
import org.opendaylight.yangtools.yang.data.tree.spi.ImmutableCandidateDeleted;
import org.opendaylight.yangtools.yang.data.tree.spi.ImmutableCandidateReplaced;
import org.opendaylight.yangtools.yang.data.tree.spi.ImmutableCandidateUnmodified;

abstract class AbstractModifiedNodeBasedCandidateNode {
    private final ModifiedNode mod;
    private final TreeNode newMeta;
    private final TreeNode oldMeta;

    protected AbstractModifiedNodeBasedCandidateNode(final ModifiedNode mod, final TreeNode oldMeta,
            final TreeNode newMeta) {
        super(verifyNotNull(mod.getModificationType(), "Node %s does not have resolved modification type", mod));
        this.newMeta = newMeta;
        this.oldMeta = oldMeta;
        this.mod = mod;
    }

    final @NonNull CandidateNode toCandidateNode() {
        return switch (mod.getModificationType()) {
            case APPEARED -> new NodeBasedAppeared(this);
            case DELETE -> new ImmutableCandidateDeleted(dataBefore());
            case DISAPPEARED -> new NodeBasedDisappeared(this);
            case SUBTREE_MODIFIED -> new NodeBasedModified(this);
            case UNMODIFIED -> new ImmutableCandidateUnmodified(dataAfter());
            case WRITE -> {
                final var dataBefore = dataBefore();
                yield dataBefore == null ? new ImmutableCandidateCreated(dataAfter())
                    : new ImmutableCandidateReplaced(dataBefore, dataAfter());
            }
        };
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
        return oldMeta != null && oldMeta.data() instanceof NormalizedNodeContainer
            || newMeta != null && newMeta.data() instanceof NormalizedNodeContainer;
    }

    @SuppressWarnings("unchecked")
    private static DistinctNodeContainer<PathArgument, NormalizedNode> getContainer(
            final @Nullable TreeNode meta) {
        return meta == null ? null : (DistinctNodeContainer<PathArgument, NormalizedNode>)meta.data();
    }

    final Collection<CandidateNode> candidateChildren() {
        return Collections2.transform(mod.getChildren(), mod -> childNode(mod).toCandidateNode());
    }

    final @Nullable CandidateNode candidateModifiedChild(final PathArgument arg) {
        final var child = mod.childByArg(arg);
        return child == null ? null : childNode(child).toCandidateNode();
    }

    private @NonNull ChildNode childNode(final ModifiedNode childMod) {
        final var id = childMod.getIdentifier();
        return new ChildNode(childMod, childMeta(oldMeta, id), childMeta(newMeta, id));
    }

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

<<<<<<< HEAD
    @Override
=======
    public final ModificationType modificationType() {
        return verifyNotNull(mod.getModificationType(), "Node %s does not have resolved modification type", mod);
    }

>>>>>>> 2fc250b16f (WIP: Add ImmutableCandidateNodes)
    public final NormalizedNode dataBefore() {
        return data(oldMeta);
    }

    public final NormalizedNode dataAfter() {
        return data(newMeta);
    }

    private static @Nullable NormalizedNode data(final TreeNode meta) {
        return meta == null ? null : meta.data();
    }

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

    static final class ChildNode extends AbstractModifiedNodeBasedCandidateNode {
        private ChildNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument name() {
            return getMod().getIdentifier();
        }
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.omitNullValues().add("oldMeta", oldMeta).add("newMeta", newMeta);
    }
}
