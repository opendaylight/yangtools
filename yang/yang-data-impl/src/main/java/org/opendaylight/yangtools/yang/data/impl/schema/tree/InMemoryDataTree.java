/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only snapshot of the data tree.
 */
final class InMemoryDataTree extends AbstractDataTreeTip implements TipProducingDataTree {
    private static final AtomicReferenceFieldUpdater<InMemoryDataTree, DataTreeState> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(InMemoryDataTree.class, DataTreeState.class, "state");
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTree.class);
    private static final Function<PathArgument, QName> EXTRACT_QNAME = new Function<PathArgument, QName>() {
        @Override
        public QName apply(final PathArgument input) {
            if (input instanceof AugmentationIdentifier) {
                return ((AugmentationIdentifier) input).getPossibleChildNames().iterator().next();
            } else {
                return input.getNodeType();
            }
        }
    };

    private final YangInstanceIdentifier rootPath;
    private final TreeType treeType;

    /**
     * Current data store state generation.
     */
    private volatile DataTreeState state;

    public InMemoryDataTree(final TreeNode rootNode, final TreeType treeType, final YangInstanceIdentifier rootPath, final SchemaContext schemaContext) {
        this.treeType = Preconditions.checkNotNull(treeType, "treeType");
        this.rootPath = Preconditions.checkNotNull(rootPath, "rootPath");
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

        final Collection<QName> schemaPath = Collections2.transform(rootPath.getPathArguments(), EXTRACT_QNAME);
        final SchemaNode newNode = SchemaContextUtil.findNodeInSchemaContext(newSchemaContext, schemaPath);
        if (newNode == null) {
            LOG.debug("Could not find root {} in new schema context, not upgrading", rootPath);
            return;
        }
        if (!(newNode instanceof ContainerSchemaNode)) {
            LOG.warn("Root {} resolves to non-container type {}, not upgrading", rootPath, newNode);
            return;
        }

        final SchemaAwareApplyOperation operation = SchemaAwareApplyOperation.from((ContainerSchemaNode) newNode, treeType);

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
    public void commit(final DataTreeCandidate candidate) {
        if (candidate instanceof NoopDataTreeCandidate) {
            return;
        }
        Preconditions.checkArgument(candidate instanceof InMemoryDataTreeCandidate, "Invalid candidate class %s", candidate.getClass());
        final InMemoryDataTreeCandidate c = (InMemoryDataTreeCandidate)candidate;

        if (LOG.isTraceEnabled()) {
            LOG.trace("Data Tree is {}", NormalizedNodes.toStringTree(c.getTipRoot().getData()));
        }

        final TreeNode newRoot = c.getTipRoot();
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
    public YangInstanceIdentifier getRootPath() {
        return rootPath;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("object", super.toString()).
                add("rootPath", rootPath).
                add("state", state).
                toString();
    }

    @Override
    protected TreeNode getTipRoot() {
        return state.getRoot();
    }
}
