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
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

abstract class AbstractDataTreeCandidateNode implements DataTreeCandidateNode {
    private static final Function<NormalizedNode<?, ?>, DataTreeCandidateNode> TO_DELETED_NODE = new Function<NormalizedNode<?, ?>, DataTreeCandidateNode>() {
        @Override
        public DataTreeCandidateNode apply(final NormalizedNode<?, ?> input) {
            return AbstractRecursiveCandidateNode.deleteNode(input);
        }
    };
    private static final Function<NormalizedNode<?, ?>, DataTreeCandidateNode> TO_WRITTEN_NODE = new Function<NormalizedNode<?, ?>, DataTreeCandidateNode>() {
        @Override
        public DataTreeCandidateNode apply(final NormalizedNode<?, ?> input) {
            return AbstractRecursiveCandidateNode.writeNode(input);
        }
    };

    private static Optional<NormalizedNode<?, ?>> getChild(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> container, final PathArgument identifier) {
        if (container != null) {
            return container.getChild(identifier);
        } else {
            return Optional.absent();
        }
    }

    static DataTreeCandidateNode deltaChild(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData, final PathArgument identifier) {

        final Optional<NormalizedNode<?, ?>> maybeNewChild = getChild(newData, identifier);
        final Optional<NormalizedNode<?, ?>> maybeOldChild = getChild(oldData, identifier);
        if (maybeOldChild.isPresent()) {
            final NormalizedNode<?, ?> oldChild = maybeOldChild.get();
            if (maybeNewChild.isPresent()) {
                return AbstractRecursiveCandidateNode.replaceNode(oldChild, maybeNewChild.get());
            } else {
                return TO_DELETED_NODE.apply(oldChild);
            }
        } else {
            if (maybeNewChild.isPresent()) {
                return TO_WRITTEN_NODE.apply(maybeNewChild.get());
            } else {
                return null;
            }
        }
    }

    static Collection<DataTreeCandidateNode> deltaChildren(@Nullable final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            @Nullable final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData) {
        if (newData == null) {
            return Collections2.transform(oldData.getValue(), TO_DELETED_NODE);
        }
        if (oldData == null) {
            return Collections2.transform(newData.getValue(), TO_WRITTEN_NODE);
        }

        // Create index for fast cross-references
        // FIXME: speed this up by exposing maps inside ImmutableMapNode and similar.
        final Map<PathArgument, NormalizedNode<?, ?>> oldChildren = Maps.newHashMapWithExpectedSize(oldData.getValue().size());
        for (NormalizedNode<?, ?> child : oldData.getValue()) {
            oldChildren.put(child.getIdentifier(), child);
        }

        final Collection<DataTreeCandidateNode> ret = new ArrayList<>(Math.max(oldChildren.size(), newData.getValue().size()));
        for (NormalizedNode<?, ?> child : newData.getValue()) {
            // Slight optimization iterator length
            final NormalizedNode<?, ?> oldChild = oldChildren.remove(child.getIdentifier());
            final DataTreeCandidateNode node;
            if (oldChild == null) {
                node = AbstractRecursiveCandidateNode.writeNode(child);
            } else {
                node = AbstractRecursiveCandidateNode.replaceNode(oldChild, child);
            }

            ret.add(node);
        }

        for (NormalizedNode<?, ?> child : oldChildren.values()) {
            ret.add(AbstractRecursiveCandidateNode.deleteNode(child));
        }

        return ret;
    }

    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?,?>> data;

    protected AbstractDataTreeCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        this.data = Preconditions.checkNotNull(data);
    }

    protected final Optional<NormalizedNode<?, ?>> dataOptional() {
        return Optional.<NormalizedNode<?, ?>>of(data);
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    protected final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> getData() {
        return data;
    }
}
