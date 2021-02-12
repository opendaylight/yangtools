/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class NormalizedNodePrettyTree implements Supplier<String> {
    private static final int INDENT = 4;
    private final NormalizedNode rootNode;

    NormalizedNodePrettyTree(final @NonNull NormalizedNode rootNode) {
        this.rootNode = requireNonNull(rootNode);
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public String get() {
        final StringBuilder sb = new StringBuilder();
        appendClassName(sb, rootNode).append('{').append('\n').append(" ".repeat(INDENT)).append("identifier=")
                .append(rootNode.getIdentifier());
        appendNodeValue(sb, rootNode, INDENT);
        return sb.toString();
    }

    private static void appendNodeValue(final StringBuilder sb, final NormalizedNode node, final int offset) {
        if (node instanceof NormalizedNodeContainer) {
            sb.append(", value=[");
            for (final NormalizedNode child : ((NormalizedNodeContainer<?>) node).body()) {
                appendChildNode(sb, child, node.getIdentifier(), offset + INDENT);
            }
            sb.append(']').append('}');
        } else {
            sb.append(", value=").append(node.body()).append('}');
        }
    }

    private static void appendChildNode(final StringBuilder sb, final NormalizedNode node,
            final PathArgument parentIdentifier, final int offset) {
        sb.append('\n').append(" ".repeat(offset));
        appendClassName(sb, node).append("{identifier=")
                .append(node.getIdentifier().toRelativeString(parentIdentifier));
        appendNodeValue(sb, node, offset);
    }

    private static StringBuilder appendClassName(final StringBuilder sb, final NormalizedNode node) {
        final String simpleName = node.getSimpleName();
        return sb.append(simpleName.toLowerCase(Locale.ROOT).charAt(0)).append(simpleName, 1, simpleName.length());
    }
}
