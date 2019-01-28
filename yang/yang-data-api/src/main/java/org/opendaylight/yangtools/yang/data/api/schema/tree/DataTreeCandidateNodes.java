/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

@Beta
public final class DataTreeCandidateNodes {
    private DataTreeCandidateNodes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return an empty {@link DataTreeCandidateNode} identified by specified {@link PathArgument}.
     *
     * @param identifier Node identifier
     * @return An empty DataTreeCandidateNode
     */
    public static DataTreeCandidateNode empty(final PathArgument identifier) {
        return new EmptyDataTreeCandidateNode(identifier);
    }

    @Deprecated
    public static DataTreeCandidateNode fromNormalizedNode(final NormalizedNode<?, ?> node) {
        return written(node);
    }

    public static DataTreeCandidateNode unmodified(final NormalizedNode<?, ?> node) {
        if (node instanceof NormalizedNodeContainer) {
            return new RecursiveUnmodifiedCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) node);
        }
        return new UnmodifiedLeafCandidateNode(node);
    }

    public static DataTreeCandidateNode written(final NormalizedNode<?, ?> node) {
        return new NormalizedNodeDataTreeCandidateNode(node);
    }

    public static Collection<DataTreeCandidateNode> containerDelta(
            final @Nullable NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            final @Nullable NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData) {
        checkArgument(newData != null || oldData != null,
                "No old or new data, modification type should be NONE and deltaChildren() mustn't be called.");
        if (newData == null) {
            return Collections2.transform(oldData.getValue(), DataTreeCandidateNodes::deleteNode);
        }
        if (oldData == null) {
            return Collections2.transform(newData.getValue(), DataTreeCandidateNodes::writeNode);
        }

        /*
         * This is slightly inefficient, as it requires N*F(M)+M*F(N) lookup operations, where
         * F is dependent on the implementation of NormalizedNodeContainer.getChild().
         *
         * We build the return collection by iterating over new data and looking each child up
         * in old data. Based on that we construct replaced/written nodes. We then proceed to
         * iterate over old data and looking up each child in new data.
         */
        final Collection<DataTreeCandidateNode> result = new ArrayList<>();
        for (NormalizedNode<?, ?> child : newData.getValue()) {
            final DataTreeCandidateNode node;
            final Optional<NormalizedNode<?, ?>> maybeOldChild = oldData.getChild(child.getIdentifier());

            if (maybeOldChild.isPresent()) {
                // This does not find children which have not in fact been modified, as doing that
                // reliably would require us running a full equals() on the two nodes.
                node = replaceNode(maybeOldChild.get(), child);
            } else {
                node = writeNode(child);
            }

            result.add(node);
        }

        // Process removals next, looking into new data to see if we processed it
        for (NormalizedNode<?, ?> child : oldData.getValue()) {
            if (!newData.getChild(child.getIdentifier()).isPresent()) {
                result.add(deleteNode(child));
            }
        }

        return result;
    }

    public static DataTreeCandidateNode containerDelta(
            final @Nullable NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            final @Nullable NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData,
            final @NonNull PathArgument child) {
        final Optional<NormalizedNode<?, ?>> maybeNewChild = getChild(newData, child);
        final Optional<NormalizedNode<?, ?>> maybeOldChild = getChild(oldData, child);
        if (maybeOldChild.isPresent()) {
            final NormalizedNode<?, ?> oldChild = maybeOldChild.get();
            if (maybeNewChild.isPresent()) {
                return replaceNode(oldChild, maybeNewChild.get());
            }
            return deleteNode(oldChild);
        }

        return maybeNewChild.isPresent() ? writeNode(maybeNewChild.get()) : null;
    }

    /**
     * Applies the {@code node} to the {@code cursor}, note that if the top node of (@code node} is RootNode
     * you need to use {@link #applyRootedNodeToCursor(DataTreeModificationCursor, YangInstanceIdentifier,
     * DataTreeCandidateNode) applyRootedNodeToCursor} method that works with rooted node candidates.
     *
     * @param cursor cursor from the modification we want to apply the {@code node} to
     * @param node candidate tree to apply
     */
    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
            case DELETE:
                cursor.delete(node.getIdentifier());
                break;
            case SUBTREE_MODIFIED:
                cursor.enter(node.getIdentifier());
                AbstractNodeIterator iterator = new ExitingNodeIterator(null, node.getChildNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
                break;
            case UNMODIFIED:
                // No-op
                break;
            case WRITE:
                cursor.write(node.getIdentifier(), node.getDataAfter().get());
                break;
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
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
        switch (node.getModificationType()) {
            case DELETE:
                cursor.delete(rootPath.getLastPathArgument());
                break;
            case SUBTREE_MODIFIED:
                cursor.enter(rootPath.getLastPathArgument());
                AbstractNodeIterator iterator = new ExitingNodeIterator(null, node.getChildNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
                break;
            case UNMODIFIED:
                // No-op
                break;
            case WRITE:
                cursor.write(rootPath.getLastPathArgument(), node.getDataAfter().get());
                break;
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
        }
    }

    public static void applyRootToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
            case DELETE:
                throw new IllegalArgumentException("Can not delete root.");
            case WRITE:
            case SUBTREE_MODIFIED:
                AbstractNodeIterator iterator = new RootNonExitingIterator(node.getChildNodes().iterator());
                do {
                    iterator = iterator.next(cursor);
                } while (iterator != null);
                break;
            case UNMODIFIED:
                // No-op
                break;
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
        }
    }

    private static Optional<NormalizedNode<?, ?>> getChild(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> container,
                    final PathArgument identifier) {
        return container == null ? Optional.empty() : container.getChild(identifier);
    }

    @SuppressWarnings("unchecked")
    private static DataTreeCandidateNode deleteNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveDeleteCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return new DeleteLeafCandidateNode(data);
    }


    @SuppressWarnings("unchecked")
    private static DataTreeCandidateNode replaceNode(final NormalizedNode<?, ?> oldData,
            final NormalizedNode<?, ?> newData) {
        if (oldData instanceof NormalizedNodeContainer) {
            return new RecursiveReplaceCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) oldData,
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) newData);
        }
        return new ReplaceLeafCandidateNode(oldData, newData);
    }

    @SuppressWarnings("unchecked")
    private static DataTreeCandidateNode writeNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveWriteCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return new WriteLeafCandidateNode(data);
    }

    private abstract static class AbstractNodeIterator {
        private final Iterator<DataTreeCandidateNode> iterator;

        AbstractNodeIterator(final Iterator<DataTreeCandidateNode> iterator) {
            this.iterator = requireNonNull(iterator);
        }

        AbstractNodeIterator next(final DataTreeModificationCursor cursor) {
            while (iterator.hasNext()) {
                final DataTreeCandidateNode node = iterator.next();
                switch (node.getModificationType()) {
                    case DELETE:
                        cursor.delete(node.getIdentifier());
                        break;
                    case APPEARED:
                    case DISAPPEARED:
                    case SUBTREE_MODIFIED:
                        final Collection<DataTreeCandidateNode> children = node.getChildNodes();
                        if (!children.isEmpty()) {
                            cursor.enter(node.getIdentifier());
                            return new ExitingNodeIterator(this, children.iterator());
                        }
                        break;
                    case UNMODIFIED:
                        // No-op
                        break;
                    case WRITE:
                        cursor.write(node.getIdentifier(), node.getDataAfter().get());
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
                }
            }
            exitNode(cursor);
            return getParent();
        }

        protected abstract @Nullable AbstractNodeIterator getParent();

        protected abstract void exitNode(DataTreeModificationCursor cursor);
    }

    private static final class RootNonExitingIterator extends AbstractNodeIterator {

        protected RootNonExitingIterator(final Iterator<DataTreeCandidateNode> iterator) {
            super(iterator);
        }

        @Override
        protected void exitNode(final DataTreeModificationCursor cursor) {
            // Intentional noop.
        }

        @Override
        protected AbstractNodeIterator getParent() {
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
        protected AbstractNodeIterator getParent() {
            return parent;
        }

        @Override
        protected void exitNode(final DataTreeModificationCursor cursor) {
            cursor.exit();
        }
    }
}
