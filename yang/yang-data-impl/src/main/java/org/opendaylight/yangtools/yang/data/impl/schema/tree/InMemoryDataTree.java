/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only snapshot of the data tree.
 */
final class InMemoryDataTree implements DataTree {
    private static final YangInstanceIdentifier PUBLIC_ROOT_PATH = YangInstanceIdentifier.create(Collections.<PathArgument>emptyList());
    private static final AtomicReferenceFieldUpdater<InMemoryDataTree, DataTreeState> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(InMemoryDataTree.class, DataTreeState.class, "state");
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTree.class);

    /**
     * Current data store state generation.
     */
    private volatile DataTreeState state;

    public InMemoryDataTree(final TreeNode rootNode, final SchemaContext schemaContext) {
        state = DataTreeState.createInitial(rootNode);
        if (schemaContext != null) {
            setSchemaContext(schemaContext);
        }
    }

    /*
     * This method is synchronized to guard against user attempting to install
     * multiple contexts. Otherwise it runs in a lock-free manner.
     */
    @Override
    public synchronized void setSchemaContext(final SchemaContext newSchemaContext) {
        Preconditions.checkNotNull(newSchemaContext);

        LOG.debug("Following schema contexts will be attempted {}", newSchemaContext);

        final SchemaAwareApplyOperation operation = SchemaAwareApplyOperation.from(newSchemaContext);

        DataTreeState currentState, newState;
        do {
            currentState = state;
            newState = currentState.withSchemaContext(newSchemaContext, operation);
        } while (!STATE_UPDATER.compareAndSet(this, currentState, newState));
    }

    @Override
    public InMemoryDataTreeSnapshot takeSnapshot() {
        return state.newSnapshot();
    }

    @Override
    public void validate(final DataTreeModification modification) throws DataValidationFailedException {
        Preconditions.checkArgument(modification instanceof InMemoryDataTreeModification, "Invalid modification class %s", modification.getClass());
        final InMemoryDataTreeModification m = (InMemoryDataTreeModification)modification;

        m.getStrategy().checkApplicable(PUBLIC_ROOT_PATH, m.getRootModification(), Optional.<TreeNode>of(state.getRoot()));
    }

    @Override
    public DataTreeCandidate prepare(final DataTreeModification modification) {
        Preconditions.checkArgument(modification instanceof InMemoryDataTreeModification, "Invalid modification class %s", modification.getClass());

        final InMemoryDataTreeModification m = (InMemoryDataTreeModification)modification;
        final ModifiedNode root = m.getRootModification();

        if (root.getOperation() == LogicalOperation.NONE) {
            return new NoopDataTreeCandidate(PUBLIC_ROOT_PATH, root);
        }

        final TreeNode currentRoot = state.getRoot();
        final Optional<TreeNode> newRoot = m.getStrategy().apply(m.getRootModification(),
            Optional.<TreeNode>of(currentRoot), m.getVersion());
        Preconditions.checkState(newRoot.isPresent(), "Apply strategy failed to produce root node");
        return new InMemoryDataTreeCandidate(PUBLIC_ROOT_PATH, root, currentRoot, newRoot.get());
    }

    @Override
    public void commit(final DataTreeCandidate candidate) {
        if (candidate instanceof NoopDataTreeCandidate) {
            return;
        }

        Preconditions.checkArgument(candidate instanceof InMemoryDataTreeCandidate, "Invalid candidate class %s", candidate.getClass());
        final InMemoryDataTreeCandidate c = (InMemoryDataTreeCandidate)candidate;

        if (LOG.isTraceEnabled()) {
            LOG.trace("Data Tree is {}", NormalizedNodes.toStringTree(c.getAfterRoot().getData()));
        }

        final TreeNode newRoot = c.getAfterRoot();
        DataTreeState currentState, newState;
        do {
            currentState = state;
            final TreeNode currentRoot = currentState.getRoot();
            LOG.debug("Updating datastore from {} to {}", currentRoot, newRoot);

            final TreeNode oldRoot = c.getBeforeRoot();
            Preconditions.checkState(oldRoot == currentRoot, "Store tree %s and candidate base %s differ.", currentRoot, oldRoot);

            newState = currentState.withRoot(newRoot);
            LOG.trace("Updated state from {} to {}", currentState, newState);
        } while (!STATE_UPDATER.compareAndSet(this, currentState, newState));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("object", super.toString()).add("state", state).toString();
    }
}
