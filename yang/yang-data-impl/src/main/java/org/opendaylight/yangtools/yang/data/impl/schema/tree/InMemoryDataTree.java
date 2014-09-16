/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only snapshot of the data tree.
 */
final class InMemoryDataTree implements DataTree {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTree.class);
    private static final YangInstanceIdentifier PUBLIC_ROOT_PATH = YangInstanceIdentifier.builder().build();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final LatestOperationHolder operationHolder = new LatestOperationHolder();
    private SchemaContext currentSchemaContext;
    private TreeNode rootNode;

    public InMemoryDataTree(final TreeNode rootNode, final SchemaContext schemaContext) {
        this.rootNode = Preconditions.checkNotNull(rootNode);

        if (schemaContext != null) {
            // Also sets applyOper
            setSchemaContext(schemaContext);
        }
    }

    @Override
    public void setSchemaContext(final SchemaContext newSchemaContext) {
        Preconditions.checkNotNull(newSchemaContext);

        LOG.info("Attempting to install schema contexts");
        LOG.debug("Following schema contexts will be attempted {}",newSchemaContext);

        /*
         * FIXME: we should walk the schema contexts, both current and new and see
         *        whether they are compatible here. Reject incompatible changes.
         */

        // Instantiate new apply operation, this still may fail
        final ModificationApplyOperation newApplyOper = SchemaAwareApplyOperation.from(newSchemaContext);

        // Ready to change the context now, make sure no operations are running
        rwLock.writeLock().lock();
        try {
            this.operationHolder.setCurrent(newApplyOper);
            this.currentSchemaContext = newSchemaContext;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public InMemoryDataTreeSnapshot takeSnapshot() {
        rwLock.readLock().lock();
        try {
            return new InMemoryDataTreeSnapshot(currentSchemaContext, rootNode, operationHolder.newSnapshot());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void validate(final DataTreeModification modification) throws DataValidationFailedException {
        Preconditions.checkArgument(modification instanceof InMemoryDataTreeModification, "Invalid modification class %s", modification.getClass());
        final InMemoryDataTreeModification m = (InMemoryDataTreeModification)modification;

        rwLock.readLock().lock();
        try {
            m.getStrategy().checkApplicable(PUBLIC_ROOT_PATH, m.getRootModification(), Optional.<TreeNode>of(rootNode));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public DataTreeCandidate prepare(final DataTreeModification modification) {
        Preconditions.checkArgument(modification instanceof InMemoryDataTreeModification, "Invalid modification class %s", modification.getClass());

        final InMemoryDataTreeModification m = (InMemoryDataTreeModification)modification;
        final ModifiedNode root = m.getRootModification();

        if (root.getType() == ModificationType.UNMODIFIED) {
            return new NoopDataTreeCandidate(PUBLIC_ROOT_PATH, root);
        }

        rwLock.writeLock().lock();
        try {
            final Optional<TreeNode> newRoot = m.getStrategy().apply(m.getRootModification(),
                Optional.<TreeNode>of(rootNode), m.getVersion());
            Preconditions.checkState(newRoot.isPresent(), "Apply strategy failed to produce root node");
            return new InMemoryDataTreeCandidate(PUBLIC_ROOT_PATH, root, rootNode, newRoot.get());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void commit(final DataTreeCandidate candidate) {
        if (candidate instanceof NoopDataTreeCandidate) {
            return;
        }

        Preconditions.checkArgument(candidate instanceof InMemoryDataTreeCandidate, "Invalid candidate class %s", candidate.getClass());
        final InMemoryDataTreeCandidate c = (InMemoryDataTreeCandidate)candidate;

        LOG.debug("Updating datastore from {} to {}", rootNode, c.getAfterRoot());

        if (LOG.isTraceEnabled()) {
            LOG.trace("Data Tree is {}", StoreUtils.toStringTree(c.getAfterRoot().getData()));
        }

        // Ready to change the context now, make sure no operations are running
        rwLock.writeLock().lock();
        try {
            Preconditions.checkState(c.getBeforeRoot() == rootNode,
                    "Store tree %s and candidate base %s differ.", rootNode, c.getBeforeRoot());
            this.rootNode = c.getAfterRoot();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
