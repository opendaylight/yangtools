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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class InMemoryDataTreeSnapshot extends AbstractCursorAware implements CursorAwareDataTreeSnapshot {
    private final RootModificationApplyOperation applyOper;
    private final SchemaContext schemaContext;
    private final TreeNode rootNode;

    InMemoryDataTreeSnapshot(final SchemaContext schemaContext, final TreeNode rootNode,
            final RootModificationApplyOperation applyOper) {
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.rootNode = Preconditions.checkNotNull(rootNode);
        this.applyOper = Preconditions.checkNotNull(applyOper);
    }

    TreeNode getRootNode() {
        return rootNode;
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final YangInstanceIdentifier path) {
        return NormalizedNodes.findNode(rootNode.getData(), path);
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        return new InMemoryDataTreeModification(this, applyOper);
    }

    @Override
    public DataTreeSnapshotCursor createCursor(@Nonnull final YangInstanceIdentifier path) {
        final Optional<NormalizedNode<?, ?>> maybeRoot = NormalizedNodes.findNode(rootNode.getData(), path);
        if (!maybeRoot.isPresent()) {
            return null;
        }

        final NormalizedNode<?, ?> root = maybeRoot.get();
        Preconditions.checkArgument(root instanceof NormalizedNodeContainer, "Child %s is not a container", path);
        return openCursor(new InMemoryDataTreeSnapshotCursor(this, path, (NormalizedNodeContainer<?, ?, ?>)root));
    }

    @Override
    public String toString() {
        return rootNode.getSubtreeVersion().toString();
    }
}