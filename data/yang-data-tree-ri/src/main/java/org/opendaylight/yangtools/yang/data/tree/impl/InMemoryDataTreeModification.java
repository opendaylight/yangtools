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
import com.google.common.base.VerifyException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ConcurrentModificationException;
import java.util.Map;
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
     * Internal state of a modification. It looks something like this:
     * <pre>{@code
     *   Open -> Sealing -> Defunct
     *                   -> Noop
     *                   -> Ready (-> AppliedToSnapshot) -> Prepared (-> Prepared)...
     * }</pre>
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
     * A transient state between {@link Open} and either one of {@link Defunct}, {@link Noop} or {@link Ready}. This is
     * a singleton on purpose, so the rootNode is not reachable from the modification until the process resolves.
     */
    @NonNullByDefault
    private static final class Sealing extends SingletonState {
        static final Sealing INSTANCE = new Sealing();
    }

    /**
     * The call to {@code ready()} has failed to complete. No further operations are allowed, except for
     * applyToCursor(), which should still work.
     */
    private record Defunct(NodeModification root, String threadName, @NonNull Throwable cause) implements State {
        Defunct {
            requireNonNull(root);
            requireNonNull(cause);
        }

        @Override
        public String toString() {
            // TODO: find proof that Thread.getName() does not report and remove this method
            return MoreObjects.toStringHelper(this).omitNullValues()
                .add("threadName", threadName)
                .add("cause", cause)
                .toString();
        }
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
     *
     * <p>A transition from this state requires holding a lock to establish either one of
     * <ul>
     *   <li>{@link AppliedToSnapshot} via {@code newModification()}</li>
     *   <li>{@link Prepared} via {@code prepare()}</li>
     * </ul>
     */
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

    /**
     * The same thing as {@link Ready}, but we have also seen a call to {@code newModification()} and will never touch
     * modification from that code path. We can transition from this state to {@link Prepared} without synchronization,
     * as state is only affected by 'write' threads, who are expected to provide external synchronization, symmetric to
     * how {@code Open} is affected only by 'read' threads.
     */
    @NonNullByDefault
    private record AppliedToSnapshot(ModifiedNode root, TreeNode applied) implements State {
        AppliedToSnapshot {
            requireNonNull(root);
            requireNonNull(applied);
        }

        @Override
        public String toString() {
            return "AppliedToSnapshot";
        }
    }

    /**
     * A ready modification has been completed {@code prepare()} step. This is a terminal state which can be updated
     * by 'write' threads.
     */
    @NonNullByDefault
    private record Prepared(ModifiedNode root, TreeNode applied) implements State {
        Prepared {
            requireNonNull(root);
            requireNonNull(applied);
        }

        @Override
        public String toString() {
            return "Prepared";
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
                LOG.trace("No-op resolveSnapshot()");
                return Map.entry(snapshot.getRootNode(), YangInstanceIdentifier.of());
            }
            case Open open -> rootNode = open.root;
            case Ready ready -> rootNode = ready.root;
            case AppliedToSnapshot applied -> rootNode = applied.root;
            case Prepared prepared -> rootNode = prepared.root;
            default -> throw illegalState(local, "access data of");
        }

        LOG.trace("Concurrent resolveSnapshot() in state {}", local);

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

        // Walk the strategy and modification trees in-sync, creating modification nodes as needed.
        //
        // If the user has provided wrong input, we may end up with a bunch of TOUCH nodes present ending with an empty
        // one, as we will throw the exception below. This fact could end up being a problem, as we would have bunch of
        // phantom operations.
        //
        // That is fine, as we will prune any empty TOUCH nodes in the last phase of the ready process.
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
            case Noop noop -> {
                // Trivial: just use the underlying modification
                LOG.trace("No-op newModification()");
                yield snapshot.newModification();
            }
            case AppliedToSnapshot applied -> concurrentNewModification(applied, applied.applied);
            case Prepared prepared -> concurrentNewModification(prepared, prepared.applied);
            case Ready ready -> lockedNewModification(ready);
            default -> throw illegalState(local, "chain on");
        };
    }

    @NonNullByDefault
    private InMemoryDataTreeModification newModification(final TreeNode rootNode) {
        return new InMemoryDataTreeSnapshot(snapshot.modelContext(), rootNode, strategyTree).newModification();
    }

    @NonNullByDefault
    private InMemoryDataTreeModification concurrentNewModification(final State observed, final TreeNode rootNode) {
        // Simple: reuse already computed
        LOG.trace("Concurrent newModification() in state {}", observed);
        return newModification(rootNode);
    }

    // We have observed the need to transition from Ready to AppliedToSnapshot. The lock held here protects rootNode
    // internals and precludes concurrent validate() and newModification().
    @NonNullByDefault
    private synchronized InMemoryDataTreeModification lockedNewModification(final Ready prev) {
        // state may have changed while we were waiting to acquire the lock, so re-check to see whar are we to do
        final var local = plainState();
        LOG.trace("Locked newModification() in state {}", local);

        return switch (local) {
            case AppliedToSnapshot ready -> newModification(ready.applied);
            case Prepared prepared -> newModification(prepared.applied);
            case Ready ready -> {
                final var after = getStrategy().apply(ready.root, snapshotRoot(), version);
                if (after == null) {
                    // TODO: This precludes non-presence container as a root which completely disappears. I think we
                    //       need to supported a state when we have a non-existent root. IIRC there are other parts of
                    //       code are not ready for that happening just yet.
                    throw new IllegalStateException(
                        "Data tree root is not present, possibly removed by previous modification");
                }

                STATE.set(this, new AppliedToSnapshot(ready.root, after));
                yield newModification(after);
            }
            default -> throw new VerifyException("Unexpected transition from " + prev + " to " + local);
        };
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
        final NodeModification rootNode;
        final var local = acquireState();
        switch (local) {
            case Noop noop -> {
                LOG.trace("No-op applyToCursor()");
                return;
            }
            case Open open -> rootNode = open.root;
            case Ready ready -> rootNode = ready.root;
            case AppliedToSnapshot applied -> rootNode = applied.root;
            case Defunct defunct -> rootNode = defunct.root;
            case Prepared prepared -> rootNode = prepared.root;
            default -> throw illegalState(local, "access contents of");
        }

        LOG.trace("Concurrent applyToCursor() in state {}", local);
        applyChildren(cursor, rootNode);
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
            throw new ConcurrentModificationException(this + " state changed from " + open + " to " + witness);
        }

        LOG.trace("Ready operation started");
        ready(open.root);
    }

    @NonNullByDefault
    @SuppressWarnings("checkstyle:illegalCatch")
    private void ready(final ModifiedNode rootNode) {
        final LogicalOperation rootOperation;
        try {
            rootOperation = runReady(rootNode);
        } catch (Throwable t) {
            // failure: transition to Defunct
            finishReady(new Defunct(rootNode, Thread.currentThread().getName(), t));
            throw t;
        }

        // success: check root operation to determine if this is a no-op
        finishReady(rootOperation == LogicalOperation.NONE ? Noop.INSTANCE : new Ready(rootNode));
    }

    @VisibleForTesting
    @NonNullByDefault
    LogicalOperation runReady(final ModifiedNode rootNode) {
        var current = AbstractReadyIterator.create(rootNode, getStrategy());
        do {
            current = current.process(version);
        } while (current != null);

        return rootNode.getOperation();
    }

    @NonNullByDefault
    private void finishReady(final State nextState) {
        // Make sure all affects are visible before returning, as this object may be handed off to another thread, which
        // needs to see any HashMap.modCount mutations completed.
        //
        // nextState can be either one of Defunct, Noop or Ready. We are only publishing the ModifiedNode indirectly
        // through Ready.root, which is a final field. That implies a StoreStoreFence as noted in the final paragraph of
        // "Mixed Modes and Specializations" at https://gee.cs.oswego.edu/dl/html/j9mm.html, which should already have
        // satisfied the goal.
        //
        // Now we just publish that state to acquireState()
        STATE.setRelease(this, nextState);

        // Log only afterwards because we may be executing as part of catching an OutOfMemoryError and LOG.trace() may
        // invoke nextState().toString(), which might want to allocate memory.
        LOG.trace("Ready operation completed in state {}", nextState);
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
        final var local = acquireState();
        switch (local) {
            case Noop noop -> LOG.trace("No-op validate()");
            case Prepared prepared -> LOG.trace("Prepared validate()");
            case Ready ready -> lockedValidate(ready.root, path, current);
            case AppliedToSnapshot applied -> {
                LOG.trace("Concurrent validate()");
                validate(applied.root, path, current);
            }
            default -> throw illegalState(local, "validate");
        }
    }

    @NonNullByDefault
    private void validate(final NodeModification rootNode, final YangInstanceIdentifier path, final TreeNode current)
            throws DataValidationFailedException {
        getStrategy().checkApplicable(new ModificationPath(path), rootNode, current, version);
    }

    // Public state synchronized between newModification() and validate().
    // TODO: the two are comparable, and at some point we need to have enough state transfer to give out enough of
    //       TreeNode view to give that out of newModification().
    @NonNullByDefault
    private synchronized void lockedValidate(final NodeModification rootNode, final YangInstanceIdentifier path,
            final TreeNode current) throws DataValidationFailedException {
        LOG.trace("Locked validate()");
        validate(rootNode, path, current);
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
            case Noop noop -> {
                LOG.trace("No-op prepare()");
                yield new NoopDataTreeCandidate(YangInstanceIdentifier.of(), current);
            }
            case Ready ready -> lockedPrepare(ready, path, current);
            case AppliedToSnapshot applied -> concurrentPrepare(applied, applied.root, path, current);
            case Prepared prepared -> concurrentPrepare(prepared, prepared.root, path, current);
            default -> throw illegalState(local, "prepare");
        };
    }

    @NonNullByDefault
    private Prepared prepare(final ModifiedNode rootNode, final YangInstanceIdentifier path, final TreeNode current) {
        final var newRoot = getStrategy().apply(rootNode, current, version);
        if (newRoot == null) {
            // TODO: this precludes rooting using transient root, for example a non-presence container
            throw new IllegalStateException("Apply strategy failed to produce root node for modification " + this);
        }
        return new Prepared(rootNode, newRoot);
    }

    // TODO: Transition from AppliedToSnapshot to Prepared needs to be further fleshed out. Most notably root holds the
    //       side effects of SchemaAwareOperation.apply(), but we do not use those results at all.
    //       It feels like we should have a specialized transition when we observe this state in validate(), but doing
    //       so requires a figuring out the relationship between 'applied' and the result of 'prepare': can we just
    //       reuse the 'applied' when 'snapshot.getRootNode() == current'?
    @NonNullByDefault
    private DataTreeCandidateTip concurrentPrepare(final State prev, final ModifiedNode rootNode,
            final YangInstanceIdentifier path, final TreeNode current) {
        LOG.trace("Concurrent prepare() in state {}", prev);
        final var prepared = prepare(rootNode, path, current);
        final var witness = STATE.compareAndExchangeRelease(this, prev, prepared);
        if (witness != prev) {
            throw new ConcurrentModificationException(this + " changed state changed from " + prev + " to " + witness);
        }

        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), rootNode, current, prepared.applied);
    }

    // Synchronizes with newModification() to make the Ready -> Prepared transition
    @NonNullByDefault
    private synchronized DataTreeCandidateTip lockedPrepare(final Ready prev, final YangInstanceIdentifier path,
            final TreeNode current) {
        final var local = plainState();
        LOG.trace("Locked prepare() in state {}", local);

        final var rootNode = switch (local) {
            case Ready ready -> ready.root;
            case AppliedToSnapshot applied -> applied.root;
            case Prepared prepared -> throw new ConcurrentModificationException(
                this + " changed state changed from " + prev + " to " + local);
            default -> throw new VerifyException("Unexpected transition from " + prev + " to " + local);
        };

        final var prepared = prepare(rootNode, path, current);
        STATE.set(this, prepared);

        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), rootNode, current, prepared.applied);
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

    // Note: must only be called as the first action after acquiring the the lock on this object
    @NonNullByDefault
    private State plainState() {
        return verifyNotNull((State) STATE.get(this));
    }

    @NonNullByDefault
    private static IllegalStateException illegalState(final State state, final String operation) {
        throw new IllegalStateException("Attempted to " + operation + " modification in state " + state,
            state instanceof Defunct defunct ? defunct.cause : null);
    }
}
