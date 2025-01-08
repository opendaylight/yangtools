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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
     * Internal state of a modification.
     */
    @VisibleForTesting
    sealed interface State {
        // Nothing else
    }

    /**
     * Common superclass for singleton states.
     */
    private abstract static sealed class SingletonState implements State {
        @Override
        public final String toString() {
            return getClass().getSimpleName();
        }
    }

    /**
     * Initial state: the modification is open to data operations. We do not care about concurrent access: initial build
     * up is supposed to happen in a single thread. If that is not the case, it is up to the user to provide necessary
     * coordination between her threads.
     */
    @NonNullByDefault
    private record Open(ModifiedNode root) implements State {
        Open {
            requireNonNull(root);
        }

        @Override
        public String toString() {
            return "Open";
        }
    }

    /**
     * A transient state between {@link Open} and either {@link Noop} or {@link Ready}. It does not retain the
     * modification on purpose, as a failure to completely seal the modification results in inconsistent state,
     * rendering the modification inoperable.
     */
    @NonNullByDefault
    private static final class Sealing extends SingletonState {
        static final Sealing INSTANCE = new Sealing();
    }

    /**
     * The call to {@code ready()} has completed successfully and there is nothing to do. This is a terminal state.
     */
    @NonNullByDefault
    private static final class Noop extends SingletonState {
        static final Noop INSTANCE = new Noop();
    }

    /**
     * The call to {@code ready()} has completed successfully. This modification may now be accessed from multiple
     * threads. Rules of access:
     * <ul>
     *   <li>threads performing data tree commit sequence (validate/prepare/commit) are considered to be the
     *       <b>write</b> threads providing forward progress on data tree ingress</li>
     *   <li>threads calling {@code newModification()} are considered to be the <b>read</b> threads piling up new work
     *       from the user</li>
     * </ul>
     *
     * <p>Since all of our state is kept on heap, we prioritize forward progress, so as to detach user state as soon
     * as possible, making it eligible for garbage collection.
     */
    // TODO: this is a terminal state for now, it needs to be further fleshed out
    @NonNullByDefault
    private record Ready(ModifiedNode root) implements State {
        Ready {
            requireNonNull(root);
        }

        @Override
        public String toString() {
            return "Ready";
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

    private InMemoryDataTreeModification(final RootApplyStrategy strategyTree,
            final InMemoryDataTreeSnapshot snapshot) {
        this.strategyTree = requireNonNull(strategyTree);
        this.snapshot = requireNonNull(snapshot);

        // Acquire a version for what ever comes out of this modification. Since the contract of Version states two
        // allocated version must be always different.
        // This allows us to use this version as a predicate on which other modifications are dependent on via
        // newModification(), as chained modifications can predicate that this is the version this data is going to be
        // known once committed.
        final var snapshotRoot = snapshotRoot();
        version = snapshotRoot.subtreeVersion().next();
        state = new Open(new ModifiedNode(snapshot.getRootNode(), getStrategy().getChildPolicy()));
    }

    @NonNullByDefault
    InMemoryDataTreeModification(final InMemoryDataTreeSnapshot snapshot, final RootApplyStrategy resolver) {
        this(resolver.snapshot(), snapshot);
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
        final var tree = resolveSnapshot(path);
        return tree == null ? Optional.empty()
            : NormalizedNodes.findNode(tree.getValue(), tree.getKey().data(), path);
    }

    @Override
    public Optional<VersionInfo> readVersionInfo(final YangInstanceIdentifier path) {
        final var tree = resolveSnapshot(path);
        return tree == null ? Optional.empty()
            : StoreTreeNodes.findNode(tree.getKey(), path.relativeTo(tree.getValue()).orElseThrow())
                .flatMap(treeNode -> Optional.ofNullable(treeNode.subtreeVersion().readInfo()));
    }

    private @Nullable Entry<TreeNode, YangInstanceIdentifier> resolveSnapshot(final YangInstanceIdentifier path) {
        final ModifiedNode rootNode;
        final var local = acquireState();
        switch (local) {
            case Noop noop -> {
                return Map.entry(snapshot.getRootNode(), YangInstanceIdentifier.of());
            }
            case Open(var root) -> rootNode = root;
            case Ready(var root) -> rootNode = root;
            default -> throw illegalState(local, "access data of");
        }

        // Walk the tree from the top, looking for the first node between root and the requested path which has been
        // modified. If no such node exists, we use the node itself.
        final var terminal = StoreTreeNodes.findClosestsOrFirstMatch(rootNode, path,
            node -> switch (node.getOperation()) {
                case DELETE, MERGE, WRITE -> true;
                case TOUCH, NONE -> false;
            });
        final var terminalPath = terminal.getKey();
        final var result = resolveSnapshot(terminalPath, terminal.getValue());
        return result == null ? null : Map.entry(result, terminalPath);
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
        if (acquireState() instanceof Open(var rootNode)) {
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
        return MoreObjects.toStringHelper(this).add("state", acquireState()).toString();
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        final var local = acquireState();
        return switch (local) {
            // Simple fast case: just use the underlying modification
            case Noop noop -> snapshot.newModification();
            case Ready(var root) -> newModification(root);
            default -> throw illegalState(local, "chain on");
        };
    }

    // synchronizes with prepare() and validate() to protect rootNode internals
    @NonNullByDefault
    private synchronized InMemoryDataTreeModification newModification(final ModifiedNode rootNode) {
        // We will use preallocated version, this means returned snapshot will  have same version each time this method
        // is called.
        final var newRoot = getStrategy().apply(rootNode, snapshotRoot(), version);
        if (newRoot == null) {
            throw new IllegalStateException("Data tree root is not present, possibly removed by previous modification");
        }
        return new InMemoryDataTreeSnapshot(snapshot.modelContext(), newRoot, strategyTree).newModification();
    }

    Version getVersion() {
        return version;
    }

    private ModifiedNode checkOpen() {
        final var local = acquireState();
        if (local instanceof Open(var root)) {
            return root;
        }
        throw new IllegalStateException("Data Tree is sealed. No further modifications allowed in state " + local);
    }

    private static void applyNode(final DataTreeModificationCursor cursor, final NodeModification node) {
        final var operation = node.getOperation();
        switch (operation) {
            case NONE -> {
                // No-op
            }
            case DELETE -> cursor.delete(node.getIdentifier());
            case MERGE -> {
                cursor.merge(node.getIdentifier(), node.getValue());
                applyNodeChildren(cursor, node);
            }
            case TOUCH -> {
                // TODO: we could improve efficiency of cursor use if we could understand nested TOUCH operations. One
                //       way of achieving that would be a proxy cursor, which would keep track of consecutive enter and
                //       exit calls and coalesce them.
                applyNodeChildren(cursor, node);
            }
            case WRITE -> {
                cursor.write(node.getIdentifier(), node.getValue());
                applyNodeChildren(cursor, node);
            }
            default -> throw new IllegalArgumentException("Unhandled node operation " + operation);
        }
    }

    private static void applyNodeChildren(final DataTreeModificationCursor cursor, final NodeModification node) {
        if (!node.isEmpty()) {
            cursor.enter(node.getIdentifier());
            applyChildren(cursor, node);
            cursor.exit();
        }
    }

    private static void applyChildren(final DataTreeModificationCursor cursor, final NodeModification node) {
        for (var child : node.getChildren()) {
            applyNode(cursor, child);
        }
    }

    @Override
    public void applyToCursor(final DataTreeModificationCursor cursor) {
        final var local = acquireState();
        switch (local) {
            case Noop noop -> {
                // Nothing to apply
            }
            case Open(var root) -> applyChildren(cursor, root);
            case Ready(var root) -> applyChildren(cursor, root);
            default -> throw illegalState(local, "access contents of");
        }
    }

    static void checkIdentifierReferencesData(final PathArgument arg, final NormalizedNode data) {
        final var dataName = data.name();
        checkArgument(arg.equals(dataName),
            "Instance identifier references %s but data identifier is %s", arg, dataName);
    }

    private static void checkIdentifierReferencesData(final ModifiedNode rootNode, final YangInstanceIdentifier path,
            final NormalizedNode data) {
        var arg = path.getLastPathArgument();
        if (arg == null) {
            // no last argument is possible only for root
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
        final var local = acquireState();
        if (!(local instanceof Open open)) {
            throw illegalState(local, "ready");
        }

        // We want a full CAS with setVolatile() memory semantics, as we want to force happen-before for everything,
        // including whatever user code works.
        final var witness = (State) STATE.compareAndExchange(this, open, Sealing.INSTANCE);
        if (witness != open) {
            throw new ConcurrentModificationException(
                "Concurrent ready of " + this + ", state changed from " + open + " to " + witness);
        }

        ready(open.root);
    }

    @NonNullByDefault
    private void ready(final ModifiedNode rootNode) {
        var current = AbstractReadyIterator.create(rootNode, getStrategy());
        do {
            current = current.process(version);
        } while (current != null);

        // check root to determine if this is a no-op modification
        final var nextState = rootNode.getOperation() == LogicalOperation.NONE ? Noop.INSTANCE : new Ready(rootNode);

        // Make sure all affects are visible before returning, as this object may be handed off to another thread, which
        // needs to see any HashMap.modCount mutations completed.
        // NOTE:
        //      - 'Ready' has a final field, which implies a StoreStoreFence as per
        //        https://gee.cs.oswego.edu/dl/html/j9mm.html
        //      - 'Noop' does not, but it logically throws away rootNode, so we should not care
        STATE.setRelease(this, nextState);
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
    synchronized void validate(final YangInstanceIdentifier path, final TreeNode current)
            throws DataValidationFailedException {
        final var local = acquireState();
        switch (local) {
            case Noop noop -> {
                // no-op
            }
            case Ready(var root) -> validate(root, path, current);
            default -> throw illegalState(local, "validate");
        }
    }

    // synchronizes with newModification() and prepare() to protect rootNode internals
    private synchronized void validate(final ModifiedNode rootNode, final YangInstanceIdentifier path,
            final TreeNode current) throws DataValidationFailedException {
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
        final var local = acquireState();
        return switch (local) {
            case Noop noop -> new NoopDataTreeCandidate(YangInstanceIdentifier.of(), current);
            case Ready(var root) -> prepare(root, path, current);
            default -> throw illegalState(local, "prepare");
        };
    }

    // synchronizes with newModification() and validate() to protect rootNode internals
    @NonNullByDefault
    private synchronized InMemoryDataTreeCandidate prepare(final ModifiedNode rootNode,
            final YangInstanceIdentifier path, final TreeNode current) {
        final var newRoot = getStrategy().apply(rootNode, current, version);
        if (newRoot == null) {
            // FIXME: this should be a VerifyException
            throw new IllegalStateException("Apply strategy failed to produce root node for modification " + this);
        }
        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), rootNode, current, newRoot);
    }

    /**
     * Get current state with semantics of {@link VarHandle#getAcquire(Object...)}.
     *
     * @return A {@link State} object
     */
    @VisibleForTesting
    @NonNullByDefault
    TreeNode snapshotRoot() {
        return snapshot.getRootNode();
    }

    @VisibleForTesting
    @NonNullByDefault
    State acquireState() {
        return verifyNotNull((State) STATE.getAcquire(this));
    }

    @NonNullByDefault
    private static IllegalStateException illegalState(final State state, final String operation) {
        throw new IllegalStateException("Attempted to " + operation + " modification in state " + state);
    }
}
