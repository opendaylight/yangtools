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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
public final class DataTreeCandidateNodes {
    private DataTreeCandidateNodes() {
        throw new UnsupportedOperationException();
    }

    public static DataTreeCandidateNode fromNormalizedNode(final NormalizedNode<?, ?> node) {
        return new NormalizedNodeDataTreeCandidateNode(node);
    }

    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidateNode node) {
        switch (node.getModificationType()) {
        case DELETE:
            cursor.delete(node.getIdentifier());
            break;
        case SUBTREE_MODIFIED:
            cursor.enter(node.getIdentifier());
            NodeIterator iterator = new NodeIterator(null, node.getChildNodes().iterator());
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

    private static final class NodeIterator {
        private final Iterator<DataTreeCandidateNode> iterator;
        private final NodeIterator parent;

        NodeIterator(final NodeIterator parent, final Iterator<DataTreeCandidateNode> iterator) {
            this.parent = Preconditions.checkNotNull(parent);
            this.iterator = Preconditions.checkNotNull(iterator);
        }

        NodeIterator next(final DataTreeModificationCursor cursor) {
            while (iterator.hasNext()) {
                final DataTreeCandidateNode node = iterator.next();
                switch (node.getModificationType()) {
                case DELETE:
                    cursor.delete(node.getIdentifier());
                    break;
                case SUBTREE_MODIFIED:
                    final Iterator<DataTreeCandidateNode> childIterator = node.getChildNodes().iterator();
                    if (childIterator.hasNext()) {
                        cursor.enter(node.getIdentifier());
                        return new NodeIterator(this, childIterator);
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

            cursor.exit();
            return parent;
        }
    }
}
