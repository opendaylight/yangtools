/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InMemoryDataTreeModificationPrettyTree implements Supplier<String> {
    private static final int INDENT = 4;

    private final @NonNull ModifiedNode rootNode;

    InMemoryDataTreeModificationPrettyTree(final @NonNull ModifiedNode rootNode) {
        this.rootNode = requireNonNull(rootNode);
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public String get() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MutableDataTree [\n");
        appendIndent(sb, INDENT).append("modification=");
        appendChildNode(sb, rootNode, null, 2 * INDENT).append(']');
        return sb.toString();
    }

    private static StringBuilder appendIndent(final StringBuilder sb, final int offset) {
        for (int i = 0; i < offset; i++) {
            sb.append(' ');
        }
        return sb;
    }

    private static StringBuilder appendChildNode(final StringBuilder sb, final ModifiedNode node,
            final PathArgument parentIdentifier, final int offset) {
        sb.append("ModifiedNode{\n");
        appendIndent(sb, offset).append("identifier=").append(node.getIdentifier().toRelativeString(parentIdentifier));
        sb.append(", operation=").append(node.getOperation());

        if (node.getModificationType() != null) {
            sb.append(", modificationType=").append(node.getModificationType());
        }

        return appendNodeValue(sb, node, offset).append('}');
    }

    private static StringBuilder appendNodeValue(final StringBuilder sb, final ModifiedNode node, final int offset) {
        final PathArgument identifier = node.getIdentifier();
        final Collection<ModifiedNode> children = node.getChildren();
        if (!children.isEmpty()) {
            for (final ModifiedNode child : children) {
                sb.append(", childModification={\n");
                appendIndent(sb, offset + INDENT)
                        .append(child.getIdentifier().toRelativeString(identifier)).append('=');
                appendChildNode(sb, child, identifier,offset + 2 * INDENT).append('}');
            }
        }

        return sb;
    }
}
