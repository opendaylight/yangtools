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

    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidate candidate) {
        DataTreeCandidateNodes.applyToCursor(cursor, candidate.getRootNode());
    }

    public static void applyToModification(final DataTreeModification modification, final DataTreeCandidate candidate) {
        if (modification instanceof CursorAwareDataTreeModification) {
            try (DataTreeModificationCursor cursor = ((CursorAwareDataTreeModification) modification).createCursor(candidate.getRootPath())) {
                applyToCursor(cursor, candidate);
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
}
