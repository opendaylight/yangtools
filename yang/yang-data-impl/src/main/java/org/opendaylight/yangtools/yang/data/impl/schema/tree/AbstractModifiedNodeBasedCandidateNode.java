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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;


abstract class AbstractModifiedNodeBasedCandidateNode implements DataTreeCandidateNode {
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

    private DataTreeCandidateNode childNode(final ModifiedNode input) {
        final PathArgument id = input.getIdentifier();
        return createChildNode(input, childMeta(oldMeta, id), childMeta(newMeta, id));
    }

    private static DataTreeCandidateNode createChildNode(@Nonnull final ModifiedNode input, @Nullable final TreeNode oldMeta, @Nullable final TreeNode newMeta) {
        return new ChildNode(input, oldMeta, newMeta);
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(mod.getChildren(), new Function<ModifiedNode, DataTreeCandidateNode>() {
            @Override
            public DataTreeCandidateNode apply(final ModifiedNode input) {
                return childNode(input);
            }
        });
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
        final Optional<ModifiedNode> childMod = mod.getChild(identifier);
        if (childMod.isPresent()) {
            return childNode(childMod.get());
        }
        return null;
    }

    static final class ChildNode extends AbstractModifiedNodeBasedCandidateNode {
        public ChildNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument getIdentifier() {
            return getMod().getIdentifier();
        }
    }
}