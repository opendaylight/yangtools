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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.spi.PrettyIndent;

final class NormalizedNodePrettyTree implements Supplier<String> {
    private final @NonNull NormalizedNode node;
    private final @NonNull Class<?> type;
    private final @Nullable PathArgument parentIdentifier;
    private final int initialOffset;

    NormalizedNodePrettyTree(final @NonNull NormalizedNode node, final @NonNull Class<?> type) {
        this.node = requireNonNull(node);
        this.type = requireNonNull(type);
        this.parentIdentifier = null;
        this.initialOffset = 0;
    }

    NormalizedNodePrettyTree(final @NonNull NormalizedNode node, final @NonNull Class<?> type,
            final @NonNull PathArgument parentIdentifier, final int initialOffset) {
        this.node = requireNonNull(node);
        this.type = requireNonNull(type);
        this.parentIdentifier = requireNonNull(parentIdentifier);
        this.initialOffset = initialOffset;
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public String get() {
        final StringBuilder sb = PrettyIndent.indent(new StringBuilder(), initialOffset);
        appendClassName(sb, type).append("{identifier=")
                .append(node.getIdentifier().toRelativeString(parentIdentifier));
        appendNodeValue(sb, node, initialOffset);
        return sb.toString();
    }

    private static StringBuilder appendClassName(final StringBuilder sb, final Class<?> type) {
        final String simpleName = type.getSimpleName();
        return sb.append(simpleName.toLowerCase(Locale.ROOT).charAt(0)).append(simpleName, 1, simpleName.length());
    }

    private static void appendNodeValue(final StringBuilder sb, final NormalizedNode node, final int offset) {
        if (node instanceof NormalizedNodeContainer) {
            sb.append(", value=[");
            for (final NormalizedNode child : ((NormalizedNodeContainer<?>) node).body()) {
                sb.append('\n').append(child.prettyTree(node.getIdentifier(), offset + 1));
            }
            sb.append("]}");
        } else {
            sb.append(", value=").append(node.body()).append('}');
        }
    }
}
