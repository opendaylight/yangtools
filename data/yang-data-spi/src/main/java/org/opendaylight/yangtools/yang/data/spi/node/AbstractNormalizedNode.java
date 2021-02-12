/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2020 PANTHEON.tech, s.r.o
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * Abstract base class for {@link NormalizedNode} implementations.
 *
 * @param <I> Identifier type
 * @param <T> Implemented {@link NormalizedNode} specialization type
 */
@Beta
public abstract class AbstractNormalizedNode<I extends PathArgument, T extends NormalizedNode>
        extends AbstractIdentifiable<PathArgument, I> implements NormalizedNode, Immutable {
    protected AbstractNormalizedNode(final I identifier) {
        super(identifier);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final Class<T> clazz = implementedType();
        if (!clazz.isInstance(obj)) {
            return false;
        }
        final T other = clazz.cast(obj);
        return getIdentifier().equals(other.getIdentifier()) && valueEquals(other);
    }

    @Override
    public final int hashCode() {
        return 31 * getIdentifier().hashCode() + valueHashCode();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("body", body());
    }

    protected abstract @NonNull Class<T> implementedType();

    protected abstract int valueHashCode();

    protected abstract boolean valueEquals(@NonNull T other);

    @Override
    @NonNull public Supplier<String> prettyTree() {
        return new NormalizedNodePrettyTree();
    }

    public final class NormalizedNodePrettyTree implements Supplier<String> {
        private static final int INDENT = 4;

        @Override
        public String toString() {
            return get();
        }

        @Override
        public String get() {
            final StringBuilder sb = new StringBuilder();
            toStringTree(sb, AbstractNormalizedNode.this, null, 0);
            return sb.toString();
        }

        private void toStringTree(final StringBuilder sb,
                final NormalizedNode node, final String parentNamespace, final int offset) {
            final String identifier = node.getIdentifier().toString();
            final String namespace = node.getIdentifier().getNodeType().getNamespace().toString();
            final String revision = node.getIdentifier().getNodeType().getRevision()
                    .map(Revision::toString).orElse(null);

            sb.append("\n");
            sb.append(" ".repeat(offset));
            sb.append(changeClassName(node));
            sb.append("{identifier=");
            if (namespace.equals(parentNamespace)) {
                if (revision == null) {
                    sb.append(identifier.replaceAll("\\(" + namespace + "\\)", ""));
                } else {
                    sb.append(identifier.replaceAll("\\(" + namespace + "\\?revision=" + revision + "\\)", ""));
                }
            } else {
                sb.append(identifier);
            }

            if (node instanceof NormalizedNodeContainer) {
                sb.append(", value=[");
                for (NormalizedNode child : ((NormalizedNodeContainer<?,?>)node).body()) {
                    toStringTree(sb, child, namespace, offset + INDENT);
                }
                sb.append("]}");
            } else {
                sb.append(", value=").append(node.body()).append('}');
            }
        }

        private String changeClassName(final NormalizedNode node) {
            final String nodeName = node.getClass().getSimpleName().replace("Immutable", "");
            return Character.toLowerCase(nodeName.charAt(0)) + nodeName.substring(1);
        }
    }
}
