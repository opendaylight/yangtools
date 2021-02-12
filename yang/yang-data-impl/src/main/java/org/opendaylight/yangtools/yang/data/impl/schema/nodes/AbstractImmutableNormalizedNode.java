/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

public abstract class AbstractImmutableNormalizedNode<K extends PathArgument, N extends NormalizedNode>
        extends AbstractIdentifiable<PathArgument, K> implements NormalizedNode, Immutable {
    protected AbstractImmutableNormalizedNode(final K nodeIdentifier) {
        super(nodeIdentifier);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final Class<N> clazz = implementedType();
        if (!clazz.isInstance(obj)) {
            return false;
        }
        final N other = clazz.cast(obj);
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

    @Override
    public Supplier<String> prettyTree() {
        return new NormalizedNodePrettyTree();
    }

    protected abstract @NonNull Class<N> implementedType();

    protected abstract int valueHashCode();

    protected abstract boolean valueEquals(@NonNull N other);

    public final class NormalizedNodePrettyTree implements Supplier<String> {

        @Override
        public String toString() {
            return get();
        }

        @Override
        public String get() {
            final StringBuilder sb = new StringBuilder();
            toStringTree(sb, AbstractImmutableNormalizedNode.this, 0);
            return sb.toString();
        }

        private void toStringTree(final StringBuilder sb, final NormalizedNode node, final int offset) {
            final String prefix = " ".repeat(offset);
            sb.append("\n" + prefix + changeClassName(node) + "{identifier=" + node.getIdentifier().toString());
            if (node instanceof NormalizedNodeContainer) {
                sb.append(", body=[");
                for (NormalizedNode child : ((NormalizedNodeContainer<?,?>)node).body()) {
                    toStringTree(sb, child, offset + 4);
                }
                sb.append("]}");
            } else {
                sb.append(", body=").append(node.body()).append('}');
            }
        }

        private String changeClassName(final NormalizedNode node) {
            String name = node.getClass().getSimpleName().replace("Immutable", "");
            char [] charArr = name.toCharArray();
            charArr[0] += 32;
            return new String(charArr);
        }
    }
}
