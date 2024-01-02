/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshotCursor;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class InMemoryDataTreeSnapshot extends AbstractCursorAware implements DataTreeSnapshot {
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull RootApplyStrategy applyOper;
    private final @NonNull TreeNode rootNode;

    InMemoryDataTreeSnapshot(final EffectiveModelContext modelContext, final TreeNode rootNode,
            final RootApplyStrategy applyOper) {
        this.modelContext = requireNonNull(modelContext);
        this.rootNode = requireNonNull(rootNode);
        this.applyOper = requireNonNull(applyOper);
    }

    @NonNull TreeNode getRootNode() {
        return rootNode;
    }

    @Override
    public EffectiveModelContext modelContext() {
        return modelContext;
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
    public Optional<DataTreeSnapshotCursor> openCursor(final YangInstanceIdentifier path) {
        return NormalizedNodes.findNode(rootNode.getData(), path).map(root -> {
            checkArgument(root instanceof DistinctNodeContainer, "Child %s is not a container", path);
            return openCursor(new InMemoryDataTreeSnapshotCursor(this, path, (DistinctNodeContainer<?, ?>)root));
        });
    }

    @Override
    public String toString() {
        return rootNode.getSubtreeVersion().toString();
    }
}
