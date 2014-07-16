/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Map.Entry;

import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

final class InMemoryDataTreeModification implements DataTreeModification {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTreeModification.class);
    private final RootModificationApplyOperation strategyTree;
    private final InMemoryDataTreeSnapshot snapshot;
    private final ModifiedNode rootNode;

    @GuardedBy("this")
    private boolean sealed = false;

    InMemoryDataTreeModification(final InMemoryDataTreeSnapshot snapshot, final RootModificationApplyOperation resolver) {
        this.snapshot = Preconditions.checkNotNull(snapshot);
        this.strategyTree = Preconditions.checkNotNull(resolver).snapshot();
        this.rootNode = ModifiedNode.createUnmodified(snapshot.getRootNode());
    }

    ModifiedNode getRootModification() {
        return rootNode;
    }

    ModificationApplyOperation getStrategy() {
        return strategyTree;
    }

    @Override
    public synchronized void write(final InstanceIdentifier path, final NormalizedNode<?, ?> value) {
        checkSealed();
        resolveModificationFor(path).write(value);
    }

    @Override
    public synchronized void merge(final InstanceIdentifier path, final NormalizedNode<?, ?> data) {
        checkSealed();
        mergeImpl(resolveModificationFor(path),data);
    }

    private void mergeImpl(final OperationWithModification op,final NormalizedNode<?,?> data) {

        if(data instanceof NormalizedNodeContainer<?,?,?>) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            NormalizedNodeContainer<?,?,NormalizedNode<PathArgument, ?>> dataContainer = (NormalizedNodeContainer) data;
            for(NormalizedNode<PathArgument, ?> child : dataContainer.getValue()) {
                PathArgument childId = child.getIdentifier();
                mergeImpl(op.forChild(childId), child);
            }
        }
        op.merge(data);
    }

    @Override
    public synchronized void delete(final InstanceIdentifier path) {
        checkSealed();
        resolveModificationFor(path).delete();
    }

    @Override
    public synchronized Optional<NormalizedNode<?, ?>> readNode(final InstanceIdentifier path) {
        /*
         * Walk the tree from the top, looking for the first node between root and
         * the requested path which has been modified. If no such node exists,
         * we use the node itself.
         */
        final Entry<InstanceIdentifier, ModifiedNode> entry = TreeNodeUtils.findClosest(rootNode, path);
//        final Entry<InstanceIdentifier, ModifiedNode> entry = TreeNodeUtils.findClosestsOrFirstMatch(rootNode, path, ModifiedNode.IS_TERMINAL_PREDICATE);
        final InstanceIdentifier key = entry.getKey();
        final ModifiedNode mod = entry.getValue();

        final Optional<TreeNode> result = resolveSnapshot(key, mod);
        if (result.isPresent()) {
            NormalizedNode<?, ?> data = result.get().getData();
            return NormalizedNodeUtils.findNode(key, data, path);
        } else {
            return Optional.absent();
        }
    }

    private Optional<TreeNode> resolveSnapshot(final InstanceIdentifier path,
            final ModifiedNode modification) {
        final Optional<Optional<TreeNode>> potentialSnapshot = modification.getSnapshotCache();
        if(potentialSnapshot.isPresent()) {
            return potentialSnapshot.get();
        }

        try {
            return resolveModificationStrategy(path).apply(modification, modification.getOriginal(),
                    snapshot.getRootNode().getSubtreeVersion().next());
        } catch (Exception e) {
            LOG.error("Could not create snapshot for {}:{}", path,modification,e);
            throw e;
        }
    }

    private ModificationApplyOperation resolveModificationStrategy(final InstanceIdentifier path) {
        LOG.trace("Resolving modification apply strategy for {}", path);
        if(rootNode.getType() == ModificationType.UNMODIFIED) {
            strategyTree.upgradeIfPossible();
        }

        return TreeNodeUtils.<ModificationApplyOperation>findNodeChecked(strategyTree, path);
    }

    private OperationWithModification resolveModificationFor(final InstanceIdentifier path) {
        ModifiedNode modification = rootNode;
        // We ensure strategy is present.
        ModificationApplyOperation operation = resolveModificationStrategy(path);
        for (PathArgument pathArg : path.getPathArguments()) {
            modification = modification.modifyChild(pathArg);
        }
        return OperationWithModification.from(operation, modification);
    }

    @Override
    public synchronized void ready() {
        Preconditions.checkState(!sealed, "Attempted to seal an already-sealed Data Tree.");
        sealed = true;
        rootNode.seal();
    }

    @GuardedBy("this")
    private void checkSealed() {
        Preconditions.checkState(!sealed, "Data Tree is sealed. No further modifications allowed.");
    }

    @Override
    public String toString() {
        return "MutableDataTree [modification=" + rootNode + "]";
    }

    @Override
    public synchronized DataTreeModification newModification() {
        Preconditions.checkState(sealed, "Attempted to chain on an unsealed modification");

        if(rootNode.getType() == ModificationType.UNMODIFIED) {
            return snapshot.newModification();
        }

        final TreeNode originalSnapshotRoot = snapshot.getRootNode();
        final TreeNode tempRoot = TreeNodeFactory
                .createTreeNode(originalSnapshotRoot.getData(), originalSnapshotRoot.getVersion());

        InMemoryDataTreeSnapshot tempTree = new InMemoryDataTreeSnapshot(snapshot.getSchemaContext(), tempRoot, strategyTree);

        return tempTree.newModification();
    }
}
