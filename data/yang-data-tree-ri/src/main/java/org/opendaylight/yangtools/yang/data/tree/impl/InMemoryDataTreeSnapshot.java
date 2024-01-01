/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

final class InMemoryDataTreeSnapshot extends AbstractCursorAware implements DataTreeSnapshot,
        EffectiveModelContextProvider {
    private final @NonNull RootApplyStrategy applyOper;
    private final @NonNull EffectiveModelContext schemaContext;
    private final @NonNull TreeNode rootNode;

    InMemoryDataTreeSnapshot(final EffectiveModelContext schemaContext, final TreeNode rootNode,
            final RootApplyStrategy applyOper) {
        this.schemaContext = requireNonNull(schemaContext);
        this.rootNode = requireNonNull(rootNode);
        this.applyOper = requireNonNull(applyOper);
    }

    @NonNull TreeNode getRootNode() {
        return rootNode;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return schemaContext;
    }

    @Override
    public Optional<NormalizedNode> readNode(final YangInstanceIdentifier path) {
        return NormalizedNodes.findNode(rootNode.getData(), path);
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        return new InMemoryDataTreeModification(this, applyOper);
    }

    @Override
    public InMemoryDataTreeSnapshotCursor openCursor() {
        return openCursor(new InMemoryDataTreeSnapshotCursor(this, YangInstanceIdentifier.of(),
            (DistinctNodeContainer<?, ?>) rootNode.getData()));
    }

    @Override
    public InMemoryDataTreeSnapshotCursor openCursor(final YangInstanceIdentifier rootPath) {
        return NormalizedNodes.findNode(rootNode.getData(), rootPath)
            .map(root -> {
                if (root instanceof DistinctNodeContainer<?, ?> container) {
                    return container;
                }
                throw new IllegalArgumentException("Child " + rootPath + " is not a container");
            })
            .map(root -> openCursor(new InMemoryDataTreeSnapshotCursor(this, rootPath, root)))
            .orElse(null);
    }

    @Override
    public String toString() {
        return rootNode.getSubtreeVersion().toString();
    }
}
