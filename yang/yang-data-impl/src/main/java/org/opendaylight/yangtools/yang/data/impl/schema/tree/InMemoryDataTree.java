/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only snapshot of the data tree.
 */
final class InMemoryDataTree extends AbstractDataTreeTip implements TipProducingDataTree {
    private static final AtomicReferenceFieldUpdater<InMemoryDataTree, DataTreeState> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(InMemoryDataTree.class, DataTreeState.class, "state");
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTree.class);

    private final DataTreeConfiguration treeConfig;
    private final boolean maskMandatory;

    /**
     * Current data store state generation.
     */
    private volatile DataTreeState state;

    InMemoryDataTree(final TreeNode rootNode, final DataTreeConfiguration treeConfig,
        final SchemaContext schemaContext) {
        this.treeConfig = Preconditions.checkNotNull(treeConfig, "treeConfig");
        maskMandatory = true;
        state = DataTreeState.createInitial(rootNode);
        if (schemaContext != null) {
            setSchemaContext(schemaContext);
        }
    }

    InMemoryDataTree(final TreeNode rootNode, final DataTreeConfiguration treeConfig,
            final SchemaContext schemaContext, final DataSchemaNode rootSchemaNode, final boolean maskMandatory) {
        this.treeConfig = Preconditions.checkNotNull(treeConfig, "treeConfig");
        this.maskMandatory = maskMandatory;

        state = DataTreeState.createInitial(rootNode).withSchemaContext(schemaContext, getOperation(rootSchemaNode));
    }

    private ModificationApplyOperation getOperation(final DataSchemaNode rootSchemaNode) {
        if (maskMandatory && rootSchemaNode instanceof ContainerSchemaNode) {
            return new ContainerModificationStrategy((ContainerSchemaNode) rootSchemaNode, treeConfig);
        }

        return SchemaAwareApplyOperation.from(rootSchemaNode, treeConfig);
    }

    /*
     * This method is synchronized to guard against user attempting to install
     * multiple contexts. Otherwise it runs in a lock-free manner.
     */
    @Override
    public synchronized void setSchemaContext(final SchemaContext newSchemaContext) {
        Preconditions.checkNotNull(newSchemaContext);

        LOG.debug("Following schema contexts will be attempted {}", newSchemaContext);

        final DataSchemaContextTree contextTree = DataSchemaContextTree.from(newSchemaContext);
        final DataSchemaContextNode<?> rootContextNode = contextTree.getChild(getRootPath());
        if (rootContextNode == null) {
            LOG.warn("Could not find root {} in new schema context, not upgrading", getRootPath());
            return;
        }

        final DataSchemaNode rootSchemaNode = rootContextNode.getDataSchemaNode();
        if (!(rootSchemaNode instanceof DataNodeContainer)) {
            LOG.warn("Root {} resolves to non-container type {}, not upgrading", getRootPath(), rootSchemaNode);
            return;
        }

        final ModificationApplyOperation rootNode = getOperation(rootSchemaNode);
        DataTreeState currentState;
        DataTreeState newState;
        do {
            currentState = state;
            newState = currentState.withSchemaContext(newSchemaContext, rootNode);
        } while (!STATE_UPDATER.compareAndSet(this, currentState, newState));
    }

    @Override
    public InMemoryDataTreeSnapshot takeSnapshot() {
        return state.newSnapshot();
    }

    @Override
    public void commit(final DataTreeCandidate candidate) {
        if (candidate instanceof NoopDataTreeCandidate) {
            return;
        }
        Preconditions.checkArgument(candidate instanceof InMemoryDataTreeCandidate, "Invalid candidate class %s",
            candidate.getClass());
        final InMemoryDataTreeCandidate c = (InMemoryDataTreeCandidate)candidate;

        if (LOG.isTraceEnabled()) {
            LOG.trace("Data Tree is {}", NormalizedNodes.toStringTree(c.getTipRoot().getData()));
        }

        final TreeNode newRoot = c.getTipRoot();
        DataTreeState currentState;
        DataTreeState newState;
        do {
            currentState = state;
            final TreeNode currentRoot = currentState.getRoot();
            LOG.debug("Updating datastore from {} to {}", currentRoot, newRoot);

            final TreeNode oldRoot = c.getBeforeRoot();
            if (oldRoot != currentRoot) {
                final String oldStr = simpleToString(oldRoot);
                final String currentStr = simpleToString(currentRoot);
                throw new IllegalStateException("Store tree " + currentStr + " and candidate base " + oldStr
                    + " differ.");
            }

            newState = currentState.withRoot(newRoot);
            LOG.trace("Updated state from {} to {}", currentState, newState);
        } while (!STATE_UPDATER.compareAndSet(this, currentState, newState));
    }

    private static String simpleToString(final Object obj) {
        return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
    }

    @Override
    public YangInstanceIdentifier getRootPath() {
        return treeConfig.getRootPath();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("object", super.toString())
                .add("config", treeConfig)
                .add("state", state)
                .toString();
    }

    @Override
    @Nonnull
    protected TreeNode getTipRoot() {
        return state.getRoot();
    }
}
