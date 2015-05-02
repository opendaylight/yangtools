/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class holding methods useful when dealing with {@link DataTreeCandidate} instances.
 */
@Beta
public final class DataTreeCandidates {
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidates.class);
    private DataTreeCandidates() {
        throw new UnsupportedOperationException();
    }

    public static DataTreeCandidate newDataTreeCandidate(final YangInstanceIdentifier rootPath, final DataTreeCandidateNode rootNode) {
        return new DefaultDataTreeCandidate(rootPath, rootNode);
    }

    public static DataTreeCandidate fromNormalizedNode(final YangInstanceIdentifier rootPath, final NormalizedNode<?, ?> node) {
        return new DefaultDataTreeCandidate(rootPath, new NormalizedNodeDataTreeCandidateNode(node));
    }

    public static void applyToModification(final DataTreeModification modification, final DataTreeCandidate candidate) {
        if (modification instanceof CursorAwareDataTreeModification) {
            try (DataTreeModificationCursor cursor = ((CursorAwareDataTreeModification) modification).createCursor(candidate.getRootPath())) {
                applyNode(cursor, candidate.getRootNode());
            }
        } else {
            applyNode(modification, candidate.getRootPath(), candidate.getRootNode());
        }
    }

    private static void applyNode(final DataTreeModification modification, final YangInstanceIdentifier path, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
        case DELETE:
            modification.delete(path);
            LOG.debug("Modification {} deleted path {}", modification, path);
            break;
        case SUBTREE_MODIFIED:
            LOG.debug("Modification {} modified path {}", modification, path);
            for (DataTreeCandidateNode child : node.getChildNodes()) {
                applyNode(modification, path.node(child.getIdentifier()), child);
            }
            break;
        case UNMODIFIED:
            LOG.debug("Modification {} unmodified path {}", modification, path);
            // No-op
            break;
        case WRITE:
            modification.write(path, node.getDataAfter().get());
            LOG.debug("Modification {} written path {}", modification, path);
            break;
        default:
            throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
        }
    }

    private static void applyNode(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        AbstractNodeIterator iterator = new NodeIterator(null, node);

        do {
            iterator = iterator.next(cursor);
        } while (iterator != null);
    }

    private static abstract class AbstractNodeIterator {
        private final AbstractNodeIterator parent;

        AbstractNodeIterator(final AbstractNodeIterator parent) {
            this.parent = parent;
        }

        final AbstractNodeIterator getParent() {
            return parent;
        }

        abstract AbstractNodeIterator next(final DataTreeModificationCursor cursor);
    }

    private static final class NodeIterator extends AbstractNodeIterator {
        private final DataTreeCandidateNode node;

        NodeIterator(final AbstractNodeIterator parent, final DataTreeCandidateNode node) {
            super(parent);
            this.node = Preconditions.checkNotNull(node);
        }

        @Override
        AbstractNodeIterator next(final DataTreeModificationCursor cursor) {
            switch (node.getModificationType()) {
            case DELETE:
                cursor.delete(node.getIdentifier());
                return getParent();
            case SUBTREE_MODIFIED:
                cursor.enter(node.getIdentifier());
                return new ChildIterator(getParent(), node.getChildNodes().iterator());
            case UNMODIFIED:
                // No-op
                return getParent();
            case WRITE:
                cursor.write(node.getIdentifier(), node.getDataAfter().get());
                return getParent();
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
            }
        }
    }

    private static final class ChildIterator extends AbstractNodeIterator {
        private final Iterator<DataTreeCandidateNode> children;

        ChildIterator(final AbstractNodeIterator parent, final Iterator<DataTreeCandidateNode> children) {
            super(parent);
            this.children = Preconditions.checkNotNull(children);
        }

        @Override
        AbstractNodeIterator next(final DataTreeModificationCursor cursor) {
            if (!children.hasNext()) {
                cursor.exit();
                return getParent();
            } else {
                return new NodeIterator(this, children.next());
            }
        }
    }
}
