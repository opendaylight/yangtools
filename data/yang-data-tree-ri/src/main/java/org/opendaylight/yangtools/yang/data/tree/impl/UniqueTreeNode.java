/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.DecoratingTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

/**
 * A {@link DecoratingTreeNode} maintaining a set of {@link UniqueIndex}es.
 */
@NonNullByDefault
final class UniqueTreeNode extends DecoratingTreeNode {
    private static final class Mutable extends MutableTreeNode {
        private final List<UniqueIndex.Builder> builders;
        private final MutableTreeNode base;

        Mutable(final MutableTreeNode base, final List<UniqueIndex.Builder> builders) {
            this.base = requireNonNull(base);
            this.builders = requireNonNull(builders);
        }

        @Override
        public void setSubtreeVersion(final Version subtreeVersion) {
            base.setSubtreeVersion(subtreeVersion);
        }

        @Override
        public TreeNode childByArg(final PathArgument arg) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setData(final NormalizedNode data) {
            // TODO Auto-generated method stub
        }

        @Override
        public TreeNode putChild(final TreeNode child) {
            final var prev = base.putChild(child);
            if (prev != null) {
                unindexChild(prev);
            }
            indexChild(child);
            return prev;
        }

        @Override
        public TreeNode removeChild(final PathArgument id) {
            final var removed = base.removeChild(id);
            if (removed != null) {
                unindexChild(removed);
            }
            return removed;
        }

        private void indexChild(final TreeNode child) {
            // FIXME: derive unique value and ... node identifier?
            builders.forEach(builder -> builder.addUniqueValues(null, null));
        }

        private void unindexChild(final TreeNode child) {
            // FIXME: derive unique value and ... node identifier?
            builders.forEach(builder -> builder.removeUniqueValues(null, null));
        }

        @Override
        public UniqueTreeNode seal() {
            return new UniqueTreeNode(base.seal(), builders.stream()
                .map(builder -> builder.build(IllegalStateException::new))
                .collect(Collectors.toList()));
        }
    }

    private final Object indexen;

    UniqueTreeNode(final TreeNode base, final List<UniqueIndex> indexen) {
        super(base);
        this.indexen = indexen.size() == 1 ? requireNonNull(indexen.get(0)) : indexen;
    }

    @Override
    public TreeNode childByArg(final PathArgument arg) {
        return base.childByArg(arg);
    }

    @Override
    public MutableTreeNode toMutable() {
        return new Mutable(base.toMutable(),
            indexen().stream().map(UniqueIndex::toBuilder).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private List<UniqueIndex> indexen() {
        return indexen instanceof UniqueIndex single ? List.of(single) : (List<UniqueIndex>) indexen;
    }
}
