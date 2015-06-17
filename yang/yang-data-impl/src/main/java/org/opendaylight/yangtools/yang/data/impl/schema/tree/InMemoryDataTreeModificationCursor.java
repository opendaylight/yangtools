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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

final class InMemoryDataTreeModificationCursor extends AbstractCursor<InMemoryDataTreeModification> implements DataTreeModificationCursor {
    private final Deque<OperationWithModification> stack = new ArrayDeque<>();

    InMemoryDataTreeModificationCursor(final InMemoryDataTreeModification parent, final YangInstanceIdentifier rootPath, final OperationWithModification rootOp) {
        super(parent, rootPath);
        stack.push(rootOp);
    }

    private OperationWithModification resolveChildModification(final PathArgument child) {
        getParent().upgradeIfPossible();
        final OperationWithModification op = stack.peek();

        final Optional<ModificationApplyOperation> potential = op.getApplyOperation().getChild(child);
        if (potential.isPresent()) {
            final ModificationApplyOperation operation = potential.get();
            final ModifiedNode modification = op.getModification().modifyChild(child, operation.getChildPolicy());

            return OperationWithModification.from(operation, modification);
        }

        // Node not found, construct its path
        final Collection<PathArgument> path = new ArrayList<>();
        path.addAll(getRootPath().getPathArguments());

        final Iterator<OperationWithModification> it = stack.descendingIterator();
        // Skip the first entry, as it's already accounted for in rootPath
        it.next();

        while (it.hasNext()) {
            path.add(it.next().getModification().getIdentifier());
        }

        throw new SchemaValidationFailedException(String.format("Child %s is not present in schema tree.", path));
    }

    @Override
    public void enter(final PathArgument child) {
        ensureNotClosed();
        stack.push(resolveChildModification(child));
    }

    @Override
    public void enter(final Iterable<PathArgument> path) {
        ensureNotClosed();

        int depth = 0;
        for (PathArgument child : path) {
            try {
                stack.push(resolveChildModification(child));
            } catch (SchemaValidationFailedException e) {
                // Undo what we have done
                for (int i = 0; i < depth; ++i) {
                    stack.pop();
                }
                throw e;
            }
            depth++;
        }
    }

    @Override
    public void exit(final int depth) {
        Preconditions.checkArgument(depth >= 0);
        ensureNotClosed();
        Preconditions.checkState(depth < stack.size());

        for (int i = 0; i < depth; i++) {
            stack.pop();
        }
    }

    private static Optional<NormalizedNode<?, ?>> readTreeNode(final Optional<TreeNode> node) {
        if (node.isPresent()) {
            return Optional.<NormalizedNode<?, ?>>of(node.get().getData());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final PathArgument child) {
        ensureNotClosed();

        final OperationWithModification parentOp = stack.peek();
        final Optional<ModifiedNode> maybeChild = parentOp.getModification().getChild(child);
        if (maybeChild.isPresent()) {
            final ModifiedNode childNode = maybeChild.get();

            Optional<TreeNode> snapshot = childNode.getSnapshot();
            if (snapshot == null) {
                // Snapshot is not present, force instantiation
                snapshot = parentOp.getApplyOperation().getChild(child).get().apply(childNode, childNode.getOriginal(), getParent().getVersion());
            }

            return readTreeNode(snapshot);
        }

        final ModifiedNode myMod = parentOp.getModification();
        Optional<TreeNode> snapshot = myMod.getSnapshot();
        if (snapshot == null) {
            snapshot = parentOp.apply(myMod.getOriginal(), getParent().getVersion());
        }

        if (snapshot.isPresent()) {
            return readTreeNode(snapshot.get().getChild(child));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void delete(final PathArgument child) {
        ensureNotClosed();
        resolveChildModification(child).delete();
    }

    @Override
    public void merge(final PathArgument child, final NormalizedNode<?, ?> data) {
        ensureNotClosed();
        InMemoryDataTreeModification.checkIdentifierReferencesData(child, data);
        resolveChildModification(child).merge(data);
    }

    @Override
    public void write(final PathArgument child, final NormalizedNode<?, ?> data) {
        ensureNotClosed();
        InMemoryDataTreeModification.checkIdentifierReferencesData(child, data);
        resolveChildModification(child).write(data);
    }
}
