/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import java.util.function.Supplier;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class NormalizedNodePrettyTree implements Supplier<String> {
    private static final int INDENT = 4;
    private final AbstractNormalizedNode<?, ?> rootNode;

    NormalizedNodePrettyTree(final AbstractNormalizedNode<?, ?> rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public String get() {
        final StringBuilder sb = new StringBuilder();
        toStringTree(sb, rootNode, null, 0);
        return sb.toString();
    }

    private void toStringTree(final StringBuilder sb, final AbstractNormalizedNode<?, ?> node,
            final PathArgument parentIdentifier, final int offset) {
        final PathArgument identifier = node.getIdentifier();

        final XMLNamespace namespace = identifier.getNodeType().getNamespace();
        final XMLNamespace parentNamespace = parentIdentifier == null
                ? null : parentIdentifier.getNodeType().getNamespace();

        sb.append("\n");
        sb.append(" ".repeat(offset));
        sb.append(changeClassName(node));
        sb.append("{identifier=");

        if (namespace.equals(parentNamespace)) {
            sb.append(identifier.toRelativeString(parentIdentifier));
        } else {
            sb.append(identifier);
        }

        if (node instanceof NormalizedNodeContainer) {
            sb.append(", value=[");
            for (final NormalizedNode child : ((NormalizedNodeContainer<?>) node).body()) {
                toStringTree(sb, (AbstractNormalizedNode<?, ?>) child, identifier, offset + INDENT);
            }
            sb.append("]}");
        } else {
            sb.append(", value=").append(node.body()).append('}');
        }
    }

    private String changeClassName(final AbstractNormalizedNode<?, ?> node) {
        final String simpleName = node.implementedType().getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
