/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Collection;
import java.util.function.Supplier;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

class ModifiedNodePrettyTree implements Supplier<String> {
    private static final int INDENT = 4;
    private final ModifiedNode rootNode;

    ModifiedNodePrettyTree(final ModifiedNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public String get() {
        final StringBuilder sb = new StringBuilder("MutableDataTree [");
        sb.append("\n");
        sb.append(" ".repeat(INDENT));
        sb.append("modification=");
        toStringTree(sb, rootNode, null, 2 * INDENT);
        sb.append("]");
        return sb.toString();
    }

    private void toStringTree(final StringBuilder sb, final ModifiedNode node,
            final PathArgument parentIdentifier, final int offset) {
        final PathArgument identifier = node.getIdentifier();

        final XMLNamespace namespace = identifier.getNodeType().getNamespace();
        final XMLNamespace parentNamespace = parentIdentifier == null
                ? null : parentIdentifier.getNodeType().getNamespace();

        sb.append("ModifiedNode{");
        sb.append("\n");
        sb.append(" ".repeat(offset));
        sb.append("identifier=");
        if (namespace.equals(parentNamespace)) {
            sb.append(identifier.toRelativeString(parentIdentifier));
        } else {
            sb.append(identifier);
        }

        sb.append(", operation=");
        sb.append(node.getOperation());

        if (node.getModificationType() != null) {
            sb.append(", modificationType=");
            sb.append(node.getModificationType());
        }

        final Collection<ModifiedNode> children = node.getChildren();
        if (!children.isEmpty()) {
            for (final ModifiedNode child : children) {
                sb.append(", childModification={");
                if (child.getIdentifier().getNodeType().getNamespace().equals(namespace)) {
                    sb.append(child.getIdentifier().toRelativeString(identifier));
                } else {
                    sb.append(child.getIdentifier());
                }
                sb.append("=");
                toStringTree(sb, child, identifier,offset + INDENT);
                sb.append("}");
            }
        }
        sb.append("}");
    }
}
