/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDataTreeModification extends AbstractCursorAware implements CursorAwareDataTreeModification {
    private static final AtomicIntegerFieldUpdater<InMemoryDataTreeModification> SEALED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(InMemoryDataTreeModification.class, "sealed");
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTreeModification.class);

    private final RootModificationApplyOperation strategyTree;
    private final InMemoryDataTreeSnapshot snapshot;
    private final ModifiedNode rootNode;
    private final Version version;

    private volatile int sealed = 0;

    InMemoryDataTreeModification(final InMemoryDataTreeSnapshot snapshot,
            final RootModificationApplyOperation resolver) {
        this.snapshot = Preconditions.checkNotNull(snapshot);
        this.strategyTree = Preconditions.checkNotNull(resolver).snapshot();
        this.rootNode = ModifiedNode.createUnmodified(snapshot.getRootNode(), strategyTree.getChildPolicy());

        /*
         * We could allocate version beforehand, since Version contract
         * states two allocated version must be always different.
         *
         * Preallocating version simplifies scenarios such as
         * chaining of modifications, since version for particular
         * node in modification and in data tree (if successfully
         * committed) will be same and will not change.
         */
        this.version = snapshot.getRootNode().getSubtreeVersion().next();
    }

    ModifiedNode getRootModification() {
        return rootNode;
    }

    ModificationApplyOperation getStrategy() {
        return strategyTree;
    }

    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        checkSealed();
        checkIdentifierReferencesData(path, data);
        resolveModificationFor(path).write(data);
    }

    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        checkSealed();
        checkIdentifierReferencesData(path, data);
        resolveModificationFor(path).merge(data, version);
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        checkSealed();

        resolveModificationFor(path).delete();
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final YangInstanceIdentifier path) {
        /*
         * Walk the tree from the top, looking for the first node between root and
         * the requested path which has been modified. If no such node exists,
         * we use the node itself.
         */
        final Entry<YangInstanceIdentifier, ModifiedNode> entry = StoreTreeNodes.findClosestsOrFirstMatch(rootNode,
            path, ModifiedNode.IS_TERMINAL_PREDICATE);
        final YangInstanceIdentifier key = entry.getKey();
        final ModifiedNode mod = entry.getValue();

        final Optional<TreeNode> result = resolveSnapshot(key, mod);
        if (result.isPresent()) {
            final NormalizedNode<?, ?> data = result.get().getData();
            return NormalizedNodes.findNode(key, data, path);
        }

        return Optional.empty();
    }

    private Optional<TreeNode> resolveSnapshot(final YangInstanceIdentifier path, final ModifiedNode modification) {
        final Optional<TreeNode> potentialSnapshot = modification.getSnapshot();
        if (potentialSnapshot != null) {
            return potentialSnapshot;
        }

        try {
            return resolveModificationStrategy(path).apply(modification, modification.getOriginal(), version);
        } catch (final Exception e) {
            LOG.error("Could not create snapshot for {}:{}", path, modification, e);
            throw e;
        }
    }

    void upgradeIfPossible() {
        if (rootNode.getOperation() == LogicalOperation.NONE) {
            strategyTree.upgradeIfPossible();
        }
    }

    private ModificationApplyOperation resolveModificationStrategy(final YangInstanceIdentifier path) {
        LOG.trace("Resolving modification apply strategy for {}", path);

        upgradeIfPossible();
        return StoreTreeNodes.findNodeChecked(strategyTree, path);
    }

    private OperationWithModification resolveModificationFor(final YangInstanceIdentifier path) {
        upgradeIfPossible();

        /*
         * Walk the strategy and modification trees in-sync, creating modification nodes as needed.
         *
         * If the user has provided wrong input, we may end up with a bunch of TOUCH nodes present
         * ending with an empty one, as we will throw the exception below. This fact could end up
         * being a problem, as we'd have bunch of phantom operations.
         *
         * That is fine, as we will prune any empty TOUCH nodes in the last phase of the ready
         * process.
         */
        ModificationApplyOperation operation = strategyTree;
        ModifiedNode modification = rootNode;

        int i = 1;
        for (final PathArgument pathArg : path.getPathArguments()) {
            final Optional<ModificationApplyOperation> potential = operation.getChild(pathArg);
            if (!potential.isPresent()) {
                throw new SchemaValidationFailedException(String.format("Child %s is not present in schema tree.",
                        path.getAncestor(i)));
            }
            operation = potential.get();
            ++i;

            modification = modification.modifyChild(pathArg, operation, version);
        }

        return OperationWithModification.from(operation, modification);
    }

    private void checkSealed() {
        Preconditions.checkState(sealed == 0, "Data Tree is sealed. No further modifications allowed.");
    }

    @Override
    public String toString() {
        return "MutableDataTree [modification=" + rootNode + "]";
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        Preconditions.checkState(sealed == 1, "Attempted to chain on an unsealed modification");

        if (rootNode.getOperation() == LogicalOperation.NONE) {
            // Simple fast case: just use the underlying modification
            return snapshot.newModification();
        }

        /*
         * We will use preallocated version, this means returned snapshot will
         * have same version each time this method is called.
         */
        final TreeNode originalSnapshotRoot = snapshot.getRootNode();
        final Optional<TreeNode> tempRoot = strategyTree.apply(rootNode, Optional.of(originalSnapshotRoot), version);
        Preconditions.checkState(tempRoot.isPresent(),
            "Data tree root is not present, possibly removed by previous modification");

        final InMemoryDataTreeSnapshot tempTree = new InMemoryDataTreeSnapshot(snapshot.getSchemaContext(),
            tempRoot.get(), strategyTree);
        return tempTree.newModification();
    }

    Version getVersion() {
        return version;
    }

    boolean isSealed() {
        return sealed == 1;
    }

    private static void applyChildren(final DataTreeModificationCursor cursor, final ModifiedNode node) {
        final Collection<ModifiedNode> children = node.getChildren();
        if (!children.isEmpty()) {
            cursor.enter(node.getIdentifier());
            for (final ModifiedNode child : children) {
                applyNode(cursor, child);
            }
            cursor.exit();
        }
    }

    private static void applyNode(final DataTreeModificationCursor cursor, final ModifiedNode node) {
        switch (node.getOperation()) {
        case NONE:
            break;
        case DELETE:
            cursor.delete(node.getIdentifier());
            break;
        case MERGE:
            cursor.merge(node.getIdentifier(), node.getWrittenValue());
            applyChildren(cursor, node);
            break;
        case TOUCH:
            // TODO: we could improve efficiency of cursor use if we could understand
            //       nested TOUCH operations. One way of achieving that would be a proxy
            //       cursor, which would keep track of consecutive enter and exit calls
            //       and coalesce them.
            applyChildren(cursor, node);
            break;
        case WRITE:
            cursor.write(node.getIdentifier(), node.getWrittenValue());
            applyChildren(cursor, node);
            break;
        default:
            throw new IllegalArgumentException("Unhandled node operation " + node.getOperation());
        }
    }

    @Override
    public void applyToCursor(@Nonnull final DataTreeModificationCursor cursor) {
        for (final ModifiedNode child : rootNode.getChildren()) {
            applyNode(cursor, child);
        }
    }

    static void checkIdentifierReferencesData(final PathArgument arg, final NormalizedNode<?, ?> data) {
        Preconditions.checkArgument(arg.equals(data.getIdentifier()),
            "Instance identifier references %s but data identifier is %s", arg, data.getIdentifier());
    }

    private void checkIdentifierReferencesData(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        final PathArgument arg;

        if (!path.isEmpty()) {
            arg = path.getLastPathArgument();
            Preconditions.checkArgument(arg != null, "Instance identifier %s has invalid null path argument", path);
        } else {
            arg = rootNode.getIdentifier();
        }

        checkIdentifierReferencesData(arg, data);
    }

    @Override
    public DataTreeModificationCursor createCursor(@Nonnull final YangInstanceIdentifier path) {
        final OperationWithModification op = resolveModificationFor(path);
        return openCursor(new InMemoryDataTreeModificationCursor(this, path, op));
    }

    @Override
    public void ready() {
        final boolean wasRunning = SEALED_UPDATER.compareAndSet(this, 0, 1);
        Preconditions.checkState(wasRunning, "Attempted to seal an already-sealed Data Tree.");

        AbstractReadyIterator current = AbstractReadyIterator.create(rootNode, strategyTree);
        do {
            current = current.process(version);
        } while (current != null);
    }
}
