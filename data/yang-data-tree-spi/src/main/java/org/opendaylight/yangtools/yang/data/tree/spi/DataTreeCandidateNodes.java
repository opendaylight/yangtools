/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;

@Beta
public final class DataTreeCandidateNodes {
    private DataTreeCandidateNodes() {
        // Hidden on purpose
    }

    /**
     * Return an unmodified {@link DataTreeCandidateNode} identified by specified {@link NormalizedNode}.
     *
     * @param node Unchanged normalized node
     * @return An empty DataTreeCandidateNode
     */
    public static @NonNull DataTreeCandidateNode unmodified(final NormalizedNode node) {
        return UnmodifiedDataTreeCandidateNode.of(node);
    }

    /**
     * Return a {@link DataTreeCandidateNode} pretending specified node was written without the data existing
     * beforehand.
     *
     * @param node Unchanged normalized node
     * @return An empty DataTreeCandidateNode
     * @throws NullPointerException if {@code node} is null
     */
    public static @NonNull DataTreeCandidateNode written(final NormalizedNode node) {
        return CreatedDataTreeCandidateNode.of(node);
    }

    /**
     * Return a collection of {@link DataTreeCandidateNode}s summarizing the changes between the contents of two
     * {@link NormalizedNodeContainer}s.
     *
     * @param oldData Old data container, may be null
     * @param newData New data container, may be null
     * @return Collection of changes
     */
    public static @NonNull Collection<DataTreeCandidateNode> containerDelta(
            final @Nullable DistinctNodeContainer<?, ?> oldData, final @Nullable DistinctNodeContainer<?, ?> newData) {
        if (newData == null) {
            return oldData == null ? ImmutableList.of()
                    : Collections2.transform(oldData.body(), DeletedDataTreeCandidateNode::of);
        }
        if (oldData == null) {
            return Collections2.transform(newData.body(), CreatedDataTreeCandidateNode::of);
        }

        /*
         * This is slightly inefficient, as it requires N*F(M)+M*F(N) lookup operations, where
         * F is dependent on the implementation of NormalizedNodeContainer.getChild().
         *
         * We build the return collection by iterating over new data and looking each child up
         * in old data. Based on that we construct replaced/written nodes. We then proceed to
         * iterate over old data and looking up each child in new data.
         */

        final var result = new ArrayList<DataTreeCandidateNode>();
        @SuppressWarnings("unchecked")
        final var oldCast = (DistinctNodeContainer<PathArgument, ?>) oldData;
        for (var child : newData.body()) {
            final var oldChild = oldCast.childByArg(child.name());
            result.add(oldChild == null ? CreatedDataTreeCandidateNode.of(child)
                // This does not find children which have not in fact been modified, as doing that
                // reliably would require us running a full equals() on the two nodes.
                : ReplacedDataTreeCandidateNode.of(oldChild, child));
        }

        // Process removals next, looking into new data to see if we processed it
        @SuppressWarnings("unchecked")
        final var newCast = (DistinctNodeContainer<PathArgument, ?>) newData;
        for (var child : oldData.body()) {
            if (newCast.childByArg(child.name()) == null) {
                result.add(DeletedDataTreeCandidateNode.of(child));
            }
        }

        return result;
    }

    /**
     * Return a collection of {@link DataTreeCandidateNode}s summarizing the change in a child, identified by a
     * {@link PathArgument}, between two {@link NormalizedNodeContainer}s.
     *
     * @param oldData Old data container, may be null
     * @param newData New data container, may be null
     * @return A {@link DataTreeCandidateNode} describing the change, or empty if the node is not present
     */
    public static @Nullable DataTreeCandidateNode containerDelta(
            final @Nullable DistinctNodeContainer<?, ?> oldData,
            final @Nullable DistinctNodeContainer<?, ?> newData,
            final @NonNull PathArgument child) {
        final var newChild = getChild(newData, child);
        final var oldChild = getChild(oldData, child);
        if (oldChild != null) {
            return newChild != null ? ReplacedDataTreeCandidateNode.of(oldChild, newChild)
                : DeletedDataTreeCandidateNode.of(oldChild);
        } else {
            return newChild != null ? CreatedDataTreeCandidateNode.of(newChild) : null;
        }
    }

    /**
     * Applies the {@code node} to the {@code cursor}, note that if the top node of {@code node} is RootNode
     * you need to use {@link #applyRootedNodeToCursor(DataTreeModificationCursor, YangInstanceIdentifier,
     * DataTreeCandidateNode) applyRootedNodeToCursor} method that works with rooted node candidates.
     *
     * @param cursor cursor from the modification we want to apply the {@code node} to
     * @param node candidate tree to apply
     */
    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        final var type = node.modificationType();
        switch (type) {
            case DELETE -> cursor.delete(node.name());
            case SUBTREE_MODIFIED -> {
                cursor.enter(node.name());
                AbstractNodeIterator iterator = new ExitingNodeIterator(null, node.childNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
            }
            case UNMODIFIED -> {
                // No-op
            }
            case WRITE -> cursor.write(node.name(), verifyNotNull(node.dataAfter()));
            default -> throw new IllegalArgumentException("Unsupported modification " + type);
        }
    }

    /**
     * Applies the {@code node} that is rooted(doesn't have an identifier) in tree A to tree B's {@code cursor}
     * at location specified by {@code rootPath}.
     *
     * @param cursor cursor from the modification we want to apply the {@code node} to
     * @param rootPath path in the {@code cursor}'s tree we want to apply to candidate to
     * @param node candidate tree to apply
     */
    public static void applyRootedNodeToCursor(final DataTreeModificationCursor cursor,
            final YangInstanceIdentifier rootPath, final DataTreeCandidateNode node) {
        final var type = node.modificationType();
        switch (type) {
            case DELETE -> cursor.delete(rootPath.getLastPathArgument());
            case SUBTREE_MODIFIED -> {
                cursor.enter(rootPath.getLastPathArgument());
                AbstractNodeIterator iterator = new ExitingNodeIterator(null, node.childNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
            }
            case UNMODIFIED -> {
                // No-op
            }
            case WRITE -> cursor.write(rootPath.getLastPathArgument(), verifyNotNull(node.dataAfter()));
            default -> throw new IllegalArgumentException("Unsupported modification " + type);
        }
    }

    public static void applyRootToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        final var type = node.modificationType();
        switch (type) {
            case DELETE -> throw new IllegalArgumentException("Can not delete root.");
            case WRITE, SUBTREE_MODIFIED -> {
                AbstractNodeIterator iterator = new RootNonExitingIterator(node.childNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
            }
            case UNMODIFIED -> {
                // No-op
            }
            default -> throw new IllegalArgumentException("Unsupported modification " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable NormalizedNode getChild(final DistinctNodeContainer<?, ?> container,
            final PathArgument identifier) {
        return container == null ? null : ((DistinctNodeContainer<PathArgument, ?>) container).childByArg(identifier);
    }

    private abstract static class AbstractNodeIterator {
        private final Iterator<DataTreeCandidateNode> iterator;

        AbstractNodeIterator(final Iterator<DataTreeCandidateNode> iterator) {
            this.iterator = requireNonNull(iterator);
        }

        final AbstractNodeIterator next(final DataTreeModificationCursor cursor) {
            while (iterator.hasNext()) {
                final var node = iterator.next();
                final var type = node.modificationType();
                switch (type) {
                    case DELETE -> cursor.delete(node.name());
                    case APPEARED, DISAPPEARED, SUBTREE_MODIFIED -> {
                        final var children = node.childNodes();
                        if (!children.isEmpty()) {
                            cursor.enter(node.name());
                            return new ExitingNodeIterator(this, children.iterator());
                        }
                    }
                    case UNMODIFIED -> {
                        // No-op
                    }
                    case WRITE -> cursor.write(node.name(), verifyNotNull(node.dataAfter()));
                    default -> throw new IllegalArgumentException("Unsupported modification " + type);
                }
            }
            exitNode(cursor);
            return getParent();
        }

        abstract @Nullable AbstractNodeIterator getParent();

        abstract void exitNode(DataTreeModificationCursor cursor);
    }

    private static final class RootNonExitingIterator extends AbstractNodeIterator {
        RootNonExitingIterator(final Iterator<DataTreeCandidateNode> iterator) {
            super(iterator);
        }

        @Override
        void exitNode(final DataTreeModificationCursor cursor) {
            // Intentional noop.
        }

        @Override
        AbstractNodeIterator getParent() {
            return null;
        }
    }

    private static final class ExitingNodeIterator extends AbstractNodeIterator {
        private final AbstractNodeIterator parent;

        ExitingNodeIterator(final AbstractNodeIterator parent, final Iterator<DataTreeCandidateNode> iterator) {
            super(iterator);
            this.parent = parent;
        }

        @Override
        AbstractNodeIterator getParent() {
            return parent;
        }

        @Override
        void exitNode(final DataTreeModificationCursor cursor) {
            cursor.exit();
        }
    }
}
