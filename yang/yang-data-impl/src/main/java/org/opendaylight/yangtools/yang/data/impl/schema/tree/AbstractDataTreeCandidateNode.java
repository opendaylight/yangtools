/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

abstract class AbstractDataTreeCandidateNode implements DataTreeCandidateNode {    
    static Map<PathArgument, DataTreeCandidateNode> deltaChildren(@Nullable final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            @Nullable final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData) {

        final Map<PathArgument, DataTreeCandidateNode> ret;
        if (newData == null) {
            ret = Maps.newHashMapWithExpectedSize(oldData.getValue().size());
            for (NormalizedNode<?, ?> node : oldData.getValue()) {
                ret.put(node.getIdentifier(), AbstractRecursiveCandidateNode.deleteNode(node));
            }
            return ret;
        }
        if (oldData == null) {
            ret = Maps.newHashMapWithExpectedSize(newData.getValue().size());
            for (NormalizedNode<?, ?> node : newData.getValue()) {
                ret.put(node.getIdentifier(), AbstractRecursiveCandidateNode.writeNode(node));
            }
            return ret;
        }

        // Create index for fast cross-references
        // FIXME: speed this up by exposing maps inside ImmutableMapNode and similar.
        final Map<PathArgument, NormalizedNode<?, ?>> oldChildren = Maps.newHashMapWithExpectedSize(oldData.getValue().size());
        for (NormalizedNode<?, ?> child : oldData.getValue()) {
            oldChildren.put(child.getIdentifier(), child);
        }

        ret = Maps.newHashMapWithExpectedSize(Math.max(oldChildren.size(), newData.getValue().size()));
        
        for (NormalizedNode<?, ?> child : newData.getValue()) {
            // Slight optimization iterator length
            final NormalizedNode<?, ?> oldChild = oldChildren.remove(child.getIdentifier());
            final DataTreeCandidateNode node;
            if (oldChild == null) {
                node = AbstractRecursiveCandidateNode.writeNode(child);
            } else {
                node = AbstractRecursiveCandidateNode.replaceNode(oldChild, child);
            }

            ret.put(node.getIdentifier(), node);
        }

        for (NormalizedNode<?, ?> child : oldChildren.values()) {
            final DataTreeCandidateNode node = AbstractRecursiveCandidateNode.deleteNode(child);
            ret.put(node.getIdentifier(), node);
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
