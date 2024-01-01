/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

final class InMemoryDataTreeSnapshotCursor extends AbstractCursor<InMemoryDataTreeSnapshot> {
    private final Deque<DistinctNodeContainer<?, ?>> stack = new ArrayDeque<>();

    InMemoryDataTreeSnapshotCursor(final InMemoryDataTreeSnapshot parent, final YangInstanceIdentifier rootPath,
            final DistinctNodeContainer<?, ?> normalizedNode) {
        super(parent, rootPath);
        stack.push(normalizedNode);
    }

    @Override
    public void enter(final PathArgument child) {
        final Optional<NormalizedNode> maybeChildNode = NormalizedNodes.getDirectChild(stack.peek(), child);
        checkArgument(maybeChildNode.isPresent(), "Child %s not found", child);

        final NormalizedNode childNode = maybeChildNode.orElseThrow();
        checkArgument(childNode instanceof NormalizedNodeContainer, "Child %s is not a container", child);
        stack.push((DistinctNodeContainer<?, ?>) childNode);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public void enter(final Iterable<PathArgument> path) {
        final Optional<NormalizedNode> maybeChildNode = NormalizedNodes.findNode(stack.peek(), path);
        checkArgument(maybeChildNode.isPresent(), "Child %s not found", path);

        final NormalizedNode childNode = maybeChildNode.orElseThrow();
        checkArgument(childNode instanceof NormalizedNodeContainer, "Child %s is not a container", path);

        int depth = 0;
        for (PathArgument arg : path) {
            try {
                enter(arg);
            } catch (Exception e) {
                for (int i = 0; i < depth; ++i) {
                    stack.pop();
                }
                throw new IllegalArgumentException(e);
            }

            ++depth;
        }
    }

    @Override
    public void exit(final int depth) {
        checkArgument(depth >= 0);
        checkState(depth < stack.size());

        for (int i = 0; i < depth; ++i) {
            stack.pop();
        }
    }

    @Override
    public NormalizedNode readNode(final PathArgument child) {
        return NormalizedNodes.findNode(stack.peek(), child).orElse(null);
    }
}
