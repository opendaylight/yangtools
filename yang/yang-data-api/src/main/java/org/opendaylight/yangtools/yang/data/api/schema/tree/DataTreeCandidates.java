/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
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
            applyToCursor((CursorAwareDataTreeModification) modification, candidate);
        } else {
            applyNode(modification, candidate.getRootPath(), candidate.getRootNode());
        }
    }

    private static void applyToCursor(final CursorAwareDataTreeModification modification,
            final DataTreeCandidate candidate) {
        final YangInstanceIdentifier candidatePath = candidate.getRootPath();
        // FIXME: Extend check to check candidate is root of rooted data tree
        if (candidatePath.isEmpty()) {
            try (final DataTreeModificationCursor cursor = modification.createCursor(candidatePath)) {
                applyToRootCursor(cursor, candidate.getRootNode());
            }
        } else {
            try (final DataTreeModificationCursor cursor = modification.createCursor(candidatePath.getParent())) {
                applyToParentCursor(cursor, candidate.getRootNode());
            }
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
            for (final DataTreeCandidateNode child : node.getChildNodes()) {
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

    private static void applyToParentCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
        case DELETE:
            cursor.delete(node.getIdentifier());
            break;
        case SUBTREE_MODIFIED:
            cursor.enter(node.getIdentifier());
            for (final DataTreeCandidateNode child : node.getChildNodes()) {
                    applyToParentCursor(cursor, child);
            }
            cursor.exit();
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

    private static void applyToRootCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
            case DELETE:
                throw new IllegalArgumentException("Can not apply deletion of root node.");
            case UNMODIFIED:
                // No-op
                break;
            case WRITE:
            case SUBTREE_MODIFIED:
                for (final DataTreeCandidateNode child : node.getChildNodes()) {
                    applyToParentCursor(cursor, child);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
        }
    }
}
