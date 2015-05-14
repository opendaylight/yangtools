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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Instances of this class hold the current state of a DataTree instance.
 * The need for encapsulation stems from atomic updates, which potentially change
 * multiple fields in one go.
 */
final class DataTreeState {
    private final LatestOperationHolder holder;
    private final SchemaContext schemaContext;
    private final TreeNode root;
    private final TreeType treeType;

    private DataTreeState(final TreeNode root, TreeType treeType) {
        this.root = Preconditions.checkNotNull(root);
        this.treeType = Preconditions.checkNotNull(treeType);
        holder = new LatestOperationHolder();
        schemaContext = null;
    }

    private DataTreeState(final TreeNode root,  TreeType treeType, final LatestOperationHolder holder,
                          final SchemaContext schemaContext) {
        // It should be impossible to instantiate a new root without a SchemaContext
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.holder = Preconditions.checkNotNull(holder);
        this.root = Preconditions.checkNotNull(root);
        this.treeType = Preconditions.checkNotNull(treeType);
    }

    static DataTreeState createInitial(final TreeNode root, TreeType treeType) {
        return new DataTreeState(root, treeType);
    }

    TreeNode getRoot() {
        return root;
    }

    InMemoryDataTreeSnapshot newSnapshot() {
        return new InMemoryDataTreeSnapshot(schemaContext, root, holder.newSnapshot(), treeType);
    }

    DataTreeState withSchemaContext(final SchemaContext newSchemaContext, final SchemaAwareApplyOperation operation) {
        holder.setCurrent(operation);
        return new DataTreeState(root, treeType, holder, newSchemaContext);
    }

    DataTreeState withRoot(final TreeNode newRoot) {
        return new DataTreeState(newRoot, treeType, holder, schemaContext);
    }

    @Override
    public String toString() {
        final TreeNode r = root;
        return MoreObjects.toStringHelper(this).add("data", NormalizedNodes.toStringTree(r.getData())).toString();
    }
}