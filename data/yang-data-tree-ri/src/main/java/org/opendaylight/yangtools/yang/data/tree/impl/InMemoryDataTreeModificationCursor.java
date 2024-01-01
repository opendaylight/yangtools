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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;

final class InMemoryDataTreeModificationCursor extends AbstractCursor<InMemoryDataTreeModification>
        implements DataTreeModificationCursor {
    private final Deque<OperationWithModification> stack = new ArrayDeque<>();

    InMemoryDataTreeModificationCursor(final InMemoryDataTreeModification parent, final YangInstanceIdentifier rootPath,
            final OperationWithModification rootOp) {
        super(parent, rootPath);
        stack.push(rootOp);
    }

    private OperationWithModification resolveChildModification(final PathArgument child) {
        getParent().upgradeIfPossible();

        final OperationWithModification op = stack.peek();
        final ModificationApplyOperation operation = op.getApplyOperation().childByArg(child);
        if (operation != null) {
            final ModifiedNode modification = op.getModification().modifyChild(child, operation,
                getParent().getVersion());

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
        stack.push(resolveChildModification(child));
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public void enter(final Iterable<PathArgument> path) {
        int depth = 0;
        for (PathArgument child : path) {
            try {
                stack.push(resolveChildModification(child));
            } catch (Exception e) {
                // Undo what we have done
                for (int i = 0; i < depth; ++i) {
                    stack.pop();
                }
                throw new IllegalArgumentException(e);
            }
            depth++;
        }
    }

    @Override
    public void exit(final int depth) {
        checkArgument(depth >= 0);
        checkState(depth < stack.size());

        for (int i = 0; i < depth; i++) {
            stack.pop();
        }
    }

    @Override
    public NormalizedNode readNode(final PathArgument child) {
        return stack.peek().read(child, getParent().getVersion()).orElse(null);
    }

    @Override
    public void delete(final PathArgument child) {
        ensureNotClosed();
        resolveChildModification(child).delete();
    }

    @Override
    public void merge(final PathArgument child, final NormalizedNode data) {
        ensureNotClosed();
        InMemoryDataTreeModification.checkIdentifierReferencesData(child, data);
        resolveChildModification(child).merge(data, getParent().getVersion());
    }

    @Override
    public void write(final PathArgument child, final NormalizedNode data) {
        ensureNotClosed();
        InMemoryDataTreeModification.checkIdentifierReferencesData(child, data);
        resolveChildModification(child).write(data);
    }
}
