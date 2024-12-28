/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNodes;
import org.opendaylight.yangtools.yang.data.tree.api.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateTip;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.VersionInfo;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDataTreeModification extends AbstractCursorAware implements CursorAwareDataTreeModification {
    /**
     * Internal state.
     */
    private sealed interface State {
        // Nothing else
    }

    /**
     * Internal state with an attached {@link #rootNode()}.
     */
    @NonNullByDefault
    private sealed interface WithRootNode extends State {

        ModifiedNode rootNode();
    }

    /**
     * Initial state: the modification is open to data operations.
     */
    @NonNullByDefault
    private record Open(ModifiedNode rootNode) implements WithRootNode {
        Open {
            requireNonNull(rootNode);
        }

        @Override
        public String toString() {
            return "Open";
        }
    }

    /**
     * Intermediate state between {@link Open} and {@link Sealed}. It does not retain the modification on purpose, as
     * a failure to completely seal the modification results in inconsistent state, rendering the modification
     * inoperable. We want to minimize our footprint in that case.
     */
    @NonNullByDefault
    private static final class Sealing implements State {
        static final Sealing INSTANCE = new Sealing();

        @Override
        public String toString() {
            return "Sealing";
        }
    }

    // NOTE: This is mutable state accessed from multiple threads concurrently. Rules of access:
    //       - threads performing data tree commit sequence (validate/prepare/commit) are considered to be the 'write'
    //         threads providing forward progress on data tree ingress
    //       - threads performing newModification() are considered to be the 'read' threads piling up new work from
    //         the user
    //       Since all of our state is kept on heap, we prioritize forward progress, so as to detach user state as soon
    //       as possible.
    @NonNullByDefault
    private record Sealed(ModifiedNode rootNode) implements WithRootNode {
        Sealed {
            requireNonNull(rootNode);
        }

        @Override
        public String toString() {
            return "Sealed";
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTreeModification.class);

    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(InMemoryDataTreeModification.class, "state", State.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final RootApplyStrategy strategyTree;
    private final InMemoryDataTreeSnapshot snapshot;
    private final Version version;

    // All access needs to go through STATE variable handle
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile State state;

    InMemoryDataTreeModification(final InMemoryDataTreeSnapshot snapshot, final RootApplyStrategy resolver) {
        this.snapshot = requireNonNull(snapshot);
        strategyTree = requireNonNull(resolver).snapshot();
        state = new Open(ModifiedNode.createUnmodified(snapshot.getRootNode(), getStrategy().getChildPolicy()));

        /*
         * We could allocate version beforehand, since Version contract
         * states two allocated version must be always different.
         *
         * Preallocating version simplifies scenarios such as
         * chaining of modifications, since version for particular
         * node in modification and in data tree (if successfully
         * committed) will be same and will not change.
         */
        version = snapshot.getRootNode().subtreeVersion().next();
    }

    ModificationApplyOperation getStrategy() {
        final var ret = strategyTree.delegate();
        if (ret == null) {
            throw new IllegalStateException("Schema Context is not available.");
        }
        return ret;
    }

    @Override
    public EffectiveModelContext modelContext() {
        return snapshot.modelContext();
    }

    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode data) {
        final var rootNode = checkOpen();
        checkIdentifierReferencesData(rootNode, path, data);
        resolveModificationFor(rootNode, path).write(data);
    }

    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode data) {
        final var rootNode = checkOpen();
        checkIdentifierReferencesData(rootNode, path, data);
        resolveModificationFor(rootNode, path).merge(data, version);
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        resolveModificationFor(checkOpen(), path).delete();
    }

    @Override
    public Optional<NormalizedNode> readNode(final YangInstanceIdentifier path) {
        final var terminal = resolveTerminal(path);
        final var terminalPath = terminal.getKey();

        final var result = resolveSnapshot(terminalPath, terminal.getValue());
        return result == null ? Optional.empty() : NormalizedNodes.findNode(terminalPath, result.data(), path);
    }

    @Override
    public Optional<VersionInfo> readVersionInfo(final YangInstanceIdentifier path) {
        final var terminal = resolveTerminal(path);
        final var terminalPath = terminal.getKey();

        final var result = resolveSnapshot(terminalPath, terminal.getValue());
        return result == null ? Optional.empty()
            : StoreTreeNodes.findNode(result, path.relativeTo(terminalPath).orElseThrow())
                .flatMap(treeNode -> Optional.ofNullable(treeNode.subtreeVersion().readInfo()));
    }

    private Entry<YangInstanceIdentifier, ModifiedNode> resolveTerminal(final YangInstanceIdentifier path) {
        final var local = getState();
        if (!(local instanceof WithRootNode withRootNode)) {
            throw new IllegalStateException("Cannot access data in state " + local);
        }

        /*
         * Walk the tree from the top, looking for the first node between root and the requested path which has been
         * modified. If no such node exists, we use the node itself.
         */
        return StoreTreeNodes.findClosestsOrFirstMatch(withRootNode.rootNode(), path,
            input -> switch (input.getOperation()) {
                case DELETE, MERGE, WRITE -> true;
                case TOUCH, NONE -> false;
            });
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private @Nullable TreeNode resolveSnapshot(final YangInstanceIdentifier path, final ModifiedNode modification) {
        final var potentialSnapshot = modification.getSnapshot();
        if (potentialSnapshot != null) {
            return potentialSnapshot.orElse(null);
        }

        try {
            return resolveModificationStrategy(path).apply(modification, modification.original(), version);
        } catch (Exception e) {
            LOG.error("Could not create snapshot for {}:{}", path, modification, e);
            throw e;
        }
    }

    void upgradeIfPossible() {
        if (getState() instanceof Open(var rootNode)) {
            upgradeIfPossible(rootNode);
        }
    }

    private void upgradeIfPossible(final ModifiedNode rootNode) {
        if (rootNode.getOperation() == LogicalOperation.NONE) {
            strategyTree.upgradeIfPossible();
        }
    }

    private ModificationApplyOperation resolveModificationStrategy(final YangInstanceIdentifier path) {
        LOG.trace("Resolving modification apply strategy for {}", path);

        upgradeIfPossible();
        return StoreTreeNodes.findNodeChecked(getStrategy(), path);
    }

    private OperationWithModification resolveModificationFor(final ModifiedNode rootNode,
            final YangInstanceIdentifier path) {
        upgradeIfPossible(rootNode);

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
        return MoreObjects.toStringHelper(this).add("state", getState()).toString();
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        final var local = getState();
        if (!(local instanceof Sealed(var rootNode))) {
            throw new IllegalStateException("Attempted to chain on modification in state " + local);
        }

        if (rootNode.getOperation() == LogicalOperation.NONE) {
            // Simple fast case: just use the underlying modification
            return snapshot.newModification();
        }

        /*
         * We will use preallocated version, this means returned snapshot will
         * have same version each time this method is called.
         */
        final var originalSnapshotRoot = snapshot.getRootNode();
        final var newRoot = getStrategy().apply(rootNode, originalSnapshotRoot, version);
        if (newRoot == null) {
            throw new IllegalStateException("Data tree root is not present, possibly removed by previous modification");
        }
        return new InMemoryDataTreeSnapshot(snapshot.modelContext(), newRoot, strategyTree).newModification();
    }

    Version getVersion() {
        return version;
    }

    private ModifiedNode checkOpen() {
        final var local = getState();
        if (local instanceof Open(var rootNode)) {
            return rootNode;
        }
        throw new IllegalStateException("Data Tree is sealed. No further modifications allowed in state " + local);
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
        final var local = getState();
        if (local instanceof WithRootNode withRootNode) {
            for (var child : withRootNode.rootNode().getChildren()) {
                applyNode(cursor, child);
            }
        } else {
            throw new IllegalStateException("Cannot apply in state " + local);
        }
    }

    static void checkIdentifierReferencesData(final PathArgument arg, final NormalizedNode data) {
        final var dataName = data.name();
        checkArgument(arg.equals(dataName),
            "Instance identifier references %s but data identifier is %s", arg, dataName);
    }

    private static void checkIdentifierReferencesData(final ModifiedNode rootNode, final YangInstanceIdentifier path,
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
        final var op = resolveModificationFor(checkOpen(), path);
        return Optional.of(openCursor(new InMemoryDataTreeModificationCursor(this, path, op)));
    }

    @Override
    public void ready() {
        final var local = getState();
        if (!(local instanceof Open open)) {
            throw new IllegalStateException("Attempted to ready " + this + " in state " + local);
        }

        // We want a full CAS with setVolatile() memory semantics, as we want to force happen-before for everything,
        // including whatever user code works.
        final var witness = (State) STATE.compareAndExchange(this, open, Sealing.INSTANCE);
        if (witness != open) {
            throw new IllegalStateException(
                "Concurrent ready of " + this + ", state changed from " + open + " to " + witness);
        }

        var current = AbstractReadyIterator.create(open.rootNode, getStrategy());
        do {
            current = current.process(version);
        } while (current != null);

        // Make sure all affects are visible before returning, as this object may be handed off to another thread, which
        // needs to see any HashMap.modCount mutations completed.
        STATE.setRelease(this, new Sealed(open.rootNode));
    }

    /**
     * Validate against the current root node in accordance to {@link DataTree#validate(DataTreeModification)} contract.
     *
     * @param path data tree path prefix
     * @param current root node to validate against
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if this modification is not sealed
     * @throws DataValidationFailedException if modification would result in an inconsistent data tree
     */
    @NonNullByDefault
    void validate(final YangInstanceIdentifier path, final TreeNode current) throws DataValidationFailedException {
        final var local = getState();
        if (!(local instanceof Sealed(var rootNode))) {
            // FIXME: this should be an IllegalStateException
            throw new IllegalArgumentException("Attempted to validate modification in state " + local);
        }
        getStrategy().checkApplicable(new ModificationPath(path), rootNode, current, version);
    }

    /**
     * Prepare against the current root node in accordance to {@link DataTree#prepare(DataTreeModification)} contract.
     *
     * @param path data tree path prefix
     * @param current root node to prepare against
     * @return a {@link DataTreeCandidateTip}
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if this modification is not sealed
     * @throws DataValidationFailedException if modification would result in inconsistent data tree
     */
    @NonNullByDefault
    DataTreeCandidateTip prepare(final YangInstanceIdentifier path, final TreeNode current) {
        final var local = getState();
        if (!(local instanceof Sealed(var rootNode))) {
            // FIXME: this should be an IllegalStateException
            throw new IllegalArgumentException("Attempted to prepare unsealed modification " + this);
        }

        if (rootNode.getOperation() == LogicalOperation.NONE) {
            return new NoopDataTreeCandidate(YangInstanceIdentifier.of(), rootNode, current);
        }

        final var newRoot = getStrategy().apply(rootNode, current, version);
        if (newRoot == null) {
            // FIXME: this should be a VerifyException
            throw new IllegalStateException("Apply strategy failed to produce root node for modification " + this);
        }
        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), rootNode, current, newRoot);
    }

    // getAcquire() of State
    private @NonNull State getState() {
        return verifyNotNull((State) STATE.getAcquire(this));
    }
}
