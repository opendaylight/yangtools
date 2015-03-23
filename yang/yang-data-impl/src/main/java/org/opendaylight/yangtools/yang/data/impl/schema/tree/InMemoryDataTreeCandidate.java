/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

final class InMemoryDataTreeCandidate extends AbstractDataTreeCandidate {
    private static abstract class AbstractNode implements DataTreeCandidateNode {
        private final ModifiedNode mod;
        private final TreeNode newMeta;
        private final TreeNode oldMeta;

        protected AbstractNode(final ModifiedNode mod,
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
            return new ChildNode(input, childMeta(oldMeta, id), childMeta(newMeta, id));
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
            // FIXME: BUG-2876: we should eliminate this in favor of a field in mod
            switch (mod.getOperation()) {
            case DELETE:
                return ModificationType.DELETE;
            case MERGE:
                // Merge into non-existing data is a write
                if (oldMeta == null) {
                    return ModificationType.WRITE;
                }

                // Data-based checks to narrow down types
                final NormalizedNode<?, ?> data = newMeta.getData();

                // leaf or anyxml are always written
                if (!(data instanceof NormalizedNodeContainer)) {
                    return ModificationType.WRITE;
                }

                // Unkeyed collections are always written
                if (data instanceof UnkeyedListNode || data instanceof OrderedMapNode || data instanceof OrderedLeafSetNode) {
                    return ModificationType.WRITE;
                }

                // Everything else is subtree modified
                return ModificationType.SUBTREE_MODIFIED;
            case TOUCH:
                return ModificationType.SUBTREE_MODIFIED;
            case NONE:
                return ModificationType.UNMODIFIED;
            case WRITE:
                return ModificationType.WRITE;
            }

            throw new IllegalStateException("Unhandled internal operation " + mod.getOperation());
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
            if(childMod.isPresent()) {
                return childNode(mod);
            }
            return null;
        }
    }

    private static final class ChildNode extends AbstractNode {
        public ChildNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument getIdentifier() {
            return getMod().getIdentifier();
        }
    }

    private static final class RootNode extends AbstractNode {
        public RootNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument getIdentifier() {
            throw new IllegalStateException("Attempted to get identifier of the root node");
        }
    }

    private final RootNode root;

    InMemoryDataTreeCandidate(final YangInstanceIdentifier rootPath, final ModifiedNode modificationRoot,
            final TreeNode beforeRoot, final TreeNode afterRoot) {
        super(rootPath);
        this.root = new RootNode(modificationRoot, beforeRoot, afterRoot);
    }

    TreeNode getAfterRoot() {
        return root.getNewMeta();
    }

    TreeNode getBeforeRoot() {
        return root.getOldMeta();
    }

    @Override
    public DataTreeCandidateNode getRootNode() {
        return root;
    }
}
