/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Locale;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

@Beta
public final class NormalizedNodePrettyTree implements Supplier<String> {
    private final @NonNull NormalizedNode node;

    public NormalizedNodePrettyTree(final @NonNull NormalizedNode node) {
        this.node = requireNonNull(node);
    }

    @Override
    public String get() {
        return appendNode(new StringBuilder(), 0, null, node).toString();
    }

    @Override
    public String toString() {
        return get();
    }

    // FIXME: use QNameModule instead of PathArgument
    private static StringBuilder appendNode(final StringBuilder sb, final int indent, final PathArgument parentId,
            final NormalizedNode node) {
        final String simpleName = node.contract().getSimpleName();
        final PathArgument nodeId = node.getIdentifier();

        PrettyIndent.indent(sb, indent)
            .append(simpleName.toLowerCase(Locale.ROOT).charAt(0)).append(simpleName, 1, simpleName.length())
            .append("{identifier=").append(nodeId.toRelativeString(parentId));

        if (node instanceof NormalizedNodeContainer) {
            final NormalizedNodeContainer<?> container = (NormalizedNodeContainer<?>) node;
            if (!container.isEmpty()) {
                sb.append(", value=[");
                for (final NormalizedNode child : container.body()) {
                    appendNode(sb.append('\n'), indent + 1, nodeId, child);
                }
                sb.append("]");
            }
        } else {
            sb.append(", value=").append(node.body());
        }

        return sb.append('}');
    }
}
