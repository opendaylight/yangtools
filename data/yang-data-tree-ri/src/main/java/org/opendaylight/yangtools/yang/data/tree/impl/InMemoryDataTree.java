/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only snapshot of the data tree.
 */
public final class InMemoryDataTree extends AbstractDataTreeTip implements DataTree {
    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(InMemoryDataTree.class, "state", DataTreeState.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTree.class);

    private final DataTreeConfiguration treeConfig;
    private final boolean maskMandatory;

    /**
     * Current data store state generation. All accesses need to go through {@link #STATE}
     */
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile DataTreeState state;

    public InMemoryDataTree(final TreeNode rootNode, final DataTreeConfiguration treeConfig,
            final EffectiveModelContext schemaContext) {
        this.treeConfig = requireNonNull(treeConfig, "treeConfig");
        maskMandatory = true;
        state = DataTreeState.createInitial(rootNode);
        if (schemaContext != null) {
            setEffectiveModelContext(schemaContext);
        }
    }

    public InMemoryDataTree(final TreeNode rootNode, final DataTreeConfiguration treeConfig,
            final EffectiveModelContext schemaContext, final DataSchemaNode rootSchemaNode,
            final boolean maskMandatory) {
        this.treeConfig = requireNonNull(treeConfig, "treeConfig");
        this.maskMandatory = maskMandatory;

        state = DataTreeState.createInitial(rootNode).withSchemaContext(schemaContext, getOperation(rootSchemaNode));
    }

    private ModificationApplyOperation getOperation(final DataSchemaNode rootSchemaNode) {
        if (rootSchemaNode instanceof ContainerLike rootContainerLike && maskMandatory) {
            return new ContainerModificationStrategy(rootContainerLike, treeConfig);
        }
        if (rootSchemaNode instanceof ListSchemaNode rootList) {
            final PathArgument arg = treeConfig.getRootPath().getLastPathArgument();
            if (arg instanceof NodeIdentifierWithPredicates) {
                return maskMandatory ? new MapEntryModificationStrategy(rootList, treeConfig)
                        : MapEntryModificationStrategy.of(rootList, treeConfig);
            }
        }

        try {
            return SchemaAwareApplyOperation.from(rootSchemaNode, treeConfig);
        } catch (ExcludedDataSchemaNodeException e) {
            throw new IllegalArgumentException("Root node does not belong current data tree", e);
        }
    }

    @Override
    public void setEffectiveModelContext(final EffectiveModelContext newModelContext) {
        internalSetSchemaContext(newModelContext);
    }

    /*
     * This method is synchronized to guard against user attempting to install
     * multiple contexts. Otherwise it runs in a lock-free manner.
     */
    private synchronized void internalSetSchemaContext(final EffectiveModelContext newSchemaContext) {
        requireNonNull(newSchemaContext);

        LOG.debug("Following schema contexts will be attempted {}", newSchemaContext);

        final var contextTree = DataSchemaContextTree.from(newSchemaContext);
        final var rootContextNode = contextTree.childByPath(getRootPath());
        if (rootContextNode == null) {
            LOG.warn("Could not find root {} in new schema context, not upgrading", getRootPath());
            return;
        }

        final var rootSchemaNode = rootContextNode.dataSchemaNode();
        if (!(rootSchemaNode instanceof DataNodeContainer)) {
            LOG.warn("Root {} resolves to non-container type {}, not upgrading", getRootPath(), rootSchemaNode);
            return;
        }

        final var rootNode = getOperation(rootSchemaNode);
        DataTreeState currentState;
        DataTreeState newState;
        do {
            currentState = currentState();
            newState = currentState.withSchemaContext(newSchemaContext, rootNode);
            // TODO: can we lower this to compareAndSwapRelease?
        } while (!STATE.compareAndSet(this, currentState, newState));
    }

    @Override
    public InMemoryDataTreeSnapshot takeSnapshot() {
        return currentState().newSnapshot();
    }

    @Override
    public void commit(final DataTreeCandidate candidate) {
        if (candidate instanceof NoopDataTreeCandidate) {
            return;
        }
        if (!(candidate instanceof InMemoryDataTreeCandidate c)) {
            throw new IllegalArgumentException("Invalid candidate class " + candidate.getClass());
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Data Tree is {}", NormalizedNodes.toStringTree(c.getTipRoot().getData()));
        }

        final TreeNode newRoot = c.getTipRoot();
        DataTreeState currentState;
        DataTreeState newState;
        do {
            currentState = currentState();
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
            // TODO: can we lower this to compareAndSwapRelease?
        } while (!STATE.compareAndSet(this, currentState, newState));
    }

    private static String simpleToString(final Object obj) {
        return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
    }

    private DataTreeState currentState() {
        return (DataTreeState) STATE.getAcquire(this);
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
                .add("state", currentState())
                .toString();
    }

    @Override
    protected TreeNode getTipRoot() {
        return currentState().getRoot();
    }
}
