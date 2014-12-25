/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A set of utility methods for interacting with {@link NormalizedNode} objects.
 */
@Beta
public final class NormalizedNodes {
    private static final int STRINGTREE_INDENT = 4;

    private NormalizedNodes() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Convert a data subtree under a node into a human-readable string format.
     *
     * @param node Data subtree root
     * @return String containing a human-readable form of the subtree.
     */
    public static String toStringTree(final NormalizedNode<?, ?> node) {
        final StringBuilder builder = new StringBuilder();
        toStringTree(builder, node, 0);
        return builder.toString();
    }

    private static void toStringTree(final StringBuilder builder, final NormalizedNode<?, ?> node, final int offset) {
        final String prefix = Strings.repeat(" ", offset);

        builder.append(prefix).append(toStringTree(node.getIdentifier()));
        if (node instanceof NormalizedNodeContainer<?, ?, ?>) {
            final NormalizedNodeContainer<?, ?, ?> container = (NormalizedNodeContainer<?, ?, ?>) node;

            builder.append(" {\n");
            for (NormalizedNode<?, ?> child : container.getValue()) {
                toStringTree(builder, child, offset + STRINGTREE_INDENT);
            }

            builder.append(prefix).append('}');
        } else {
            builder.append(' ').append(node.getValue());
        }
        builder.append('\n');
    }

    private static String toStringTree(final PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            StringBuilder builder = new StringBuilder();
            builder.append(identifier.getNodeType().getLocalName());
            builder.append(((NodeIdentifierWithPredicates) identifier).getKeyValues().values());
            return builder.toString();
        } else if (identifier instanceof AugmentationIdentifier) {
            return "augmentation";
        } else {
            return identifier.getNodeType().getLocalName();
        }
    }
}
