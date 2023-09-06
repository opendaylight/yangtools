/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNodes;
import org.opendaylight.yangtools.yang.data.tree.api.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDataTreeModification extends AbstractCursorAware implements CursorAwareDataTreeModification,
        EffectiveModelContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTreeModification.class);

    private final RootApplyStrategy strategyTree;
    private final InMemoryDataTreeSnapshot snapshot;
    private final ModifiedNode rootNode;
    private final Version version;

    private static final VarHandle SEALED;

    static {
        try {
            SEALED = MethodHandles.lookup().findVarHandle(InMemoryDataTreeModification.class, "sealed", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // All access needs to go through this handle
    @SuppressWarnings("unused")
    private volatile int sealed;

    InMemoryDataTreeModification(final InMemoryDataTreeSnapshot snapshot,
            final RootApplyStrategy resolver) {
        this.snapshot = requireNonNull(snapshot);
        strategyTree = requireNonNull(resolver).snapshot();
        rootNode = ModifiedNode.createUnmodified(snapshot.getRootNode(), getStrategy().getChildPolicy());

        /*
         * We could allocate version beforehand, since Version contract
         * states two allocated version must be always different.
         *
         * Preallocating version simplifies scenarios such as
         * chaining of modifications, since version for particular
         * node in modification and in data tree (if successfully
         * committed) will be same and will not change.
         */
        version = snapshot.getRootNode().getSubtreeVersion().next();
    }

    ModifiedNode getRootModification() {
        return rootNode;
    }

    ModificationApplyOperation getStrategy() {
        final var ret = strategyTree.delegate();
        if (ret == null) {
            throw new IllegalStateException("Schema Context is not available.");
        }
        return ret;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return snapshot.getEffectiveModelContext();
    }

    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode data) {
        checkOpen();
        checkIdentifierReferencesData(path, data);
        resolveModificationFor(path).write(data);
    }

    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode data) {
        checkOpen();
        checkIdentifierReferencesData(path, data);
        resolveModificationFor(path).merge(data, version);
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        checkOpen();
        resolveModificationFor(path).delete();
    }

    @Override
    public Optional<NormalizedNode> readNode(final YangInstanceIdentifier path) {
        /*
         * Walk the tree from the top, looking for the first node between root and
         * the requested path which has been modified. If no such node exists,
         * we use the node itself.
         */
        final var terminal = StoreTreeNodes.findClosestsOrFirstMatch(rootNode, path,
            input -> switch (input.getOperation()) {
                case DELETE, MERGE, WRITE -> true;
                case TOUCH, NONE -> false;
            });
        final var terminalPath = terminal.getKey();

        final var result = resolveSnapshot(terminalPath, terminal.getValue());
        if (result.isPresent()) {
            final var data = result.orElseThrow().getData();
            return NormalizedNodes.findNode(terminalPath, data, path);
        }

        return Optional.empty();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private Optional<? extends TreeNode> resolveSnapshot(final YangInstanceIdentifier path,
            final ModifiedNode modification) {
        final var potentialSnapshot = modification.getSnapshot();
        if (potentialSnapshot != null) {
            return potentialSnapshot;
        }

        try {
            return resolveModificationStrategy(path).apply(modification, modification.getOriginal(), version);
        } catch (Exception e) {
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
        return StoreTreeNodes.findNodeChecked(getStrategy(), path);
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
        var operation = getStrategy();
        var modification = rootNode;

        int depth = 1;
        for (var pathArg : path.getPathArguments()) {
            operation = operation.childByArg(pathArg);
            if (operation == null) {
                throw new SchemaValidationFailedException(String.format("Child %s is not present in schema tree.",
                        path.getAncestor(depth)));
            }
            ++depth;

            modification = modification.modifyChild(pathArg, operation, version);
        }

        return OperationWithModification.from(operation, modification);
    }

    @Override
    public String toString() {
        return "MutableDataTree [modification=" + rootNode + "]";
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        checkState(isSealed(), "Attempted to chain on an unsealed modification");

        if (rootNode.getOperation() == LogicalOperation.NONE) {
            // Simple fast case: just use the underlying modification
            return snapshot.newModification();
        }

        /*
         * We will use preallocated version, this means returned snapshot will
         * have same version each time this method is called.
         */
        final var originalSnapshotRoot = snapshot.getRootNode();
        final var tempRoot = getStrategy().apply(rootNode, Optional.of(originalSnapshotRoot), version);
        checkState(tempRoot.isPresent(), "Data tree root is not present, possibly removed by previous modification");

        final var tempTree = new InMemoryDataTreeSnapshot(snapshot.getEffectiveModelContext(), tempRoot.orElseThrow(),
            strategyTree);
        return tempTree.newModification();
    }

    Version getVersion() {
        return version;
    }

    boolean isSealed() {
        // a quick check, synchronizes *only* on the sealed field
        return (int) SEALED.getAcquire(this) != 0;
    }

    private void checkOpen() {
        if (isSealed()) {
            throw new IllegalStateException("Data Tree is sealed. No further modifications allowed.");
        }
    }

    private static void applyChildren(final DataTreeModificationCursor cursor, final ModifiedNode node) {
        if (!node.isEmpty()) {
            cursor.enter(node.getIdentifier());
            for (var child : node.getChildren()) {
                applyNode(cursor, child);
            }
            cursor.exit();
        }
    }

    private static void applyNode(final DataTreeModificationCursor cursor, final ModifiedNode node) {
        final var operation = node.getOperation();
        switch (operation) {
            case NONE -> {
                // No-op
            }
            case DELETE -> cursor.delete(node.getIdentifier());
            case MERGE -> {
                cursor.merge(node.getIdentifier(), node.getWrittenValue());
                applyChildren(cursor, node);
            }
            case TOUCH -> {
                // TODO: we could improve efficiency of cursor use if we could understand nested TOUCH operations. One
                //       way of achieving that would be a proxy cursor, which would keep track of consecutive enter and
                //       exit calls and coalesce them.
                applyChildren(cursor, node);
            }
            case WRITE -> {
                cursor.write(node.getIdentifier(), node.getWrittenValue());
                applyChildren(cursor, node);
            }
            default -> throw new IllegalArgumentException("Unhandled node operation " + operation);
        }
    }

    @Override
    public void applyToCursor(final DataTreeModificationCursor cursor) {
        for (var child : rootNode.getChildren()) {
            applyNode(cursor, child);
        }
    }

    static void checkIdentifierReferencesData(final PathArgument arg, final NormalizedNode data) {
        checkArgument(arg.equals(data.getIdentifier()),
            "Instance identifier references %s but data identifier is %s", arg, data.getIdentifier());
    }

    private void checkIdentifierReferencesData(final YangInstanceIdentifier path,
            final NormalizedNode data) {
        final PathArgument arg;
        if (!path.isEmpty()) {
            arg = path.getLastPathArgument();
            checkArgument(arg != null, "Instance identifier %s has invalid null path argument", path);
        } else {
            arg = rootNode.getIdentifier();
        }

        checkIdentifierReferencesData(arg, data);
    }

    @Override
    public Optional<DataTreeModificationCursor> openCursor(final YangInstanceIdentifier path) {
        final var op = resolveModificationFor(path);
        return Optional.of(openCursor(new InMemoryDataTreeModificationCursor(this, path, op)));
    }

    @Override
    public void ready() {
        // We want a full CAS with setVolatile() memory semantics, as we want to force happen-before for everything,
        // including whatever user code works.
        if (!SEALED.compareAndSet(this, 0, 1)) {
            throw new IllegalStateException("Attempted to seal an already-sealed Data Tree.");
        }

        var current = AbstractReadyIterator.create(rootNode, getStrategy());
        do {
            current = current.process(version);
        } while (current != null);

        // Make sure all affects are visible before returning, as this object may be handed off to another thread, which
        // needs to see any HashMap.modCount mutations completed. This is needed because isSealed() is now performing
        // only the equivalent of an acquireFence()
        VarHandle.releaseFence();
    }
}
