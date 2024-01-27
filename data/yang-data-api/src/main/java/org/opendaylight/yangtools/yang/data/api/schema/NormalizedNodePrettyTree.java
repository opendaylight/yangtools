/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.concepts.PrettyTreeAware;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

@Beta
public final class NormalizedNodePrettyTree extends PrettyTree implements Immutable {
    private final @NonNull NormalizedNode node;

    public NormalizedNodePrettyTree(final @NonNull NormalizedNode node) {
        this.node = requireNonNull(node);
    }

    @Override
    public void appendTo(final StringBuilder sb, final int depth) {
        appendNode(sb, depth, null, node);
    }

    private static void appendNode(final StringBuilder sb, final int depth, final QNameModule parentNamespace,
            final NormalizedNode node) {
        final String simpleName = node.contract().getSimpleName();
        appendIndent(sb, depth);
        sb.append(simpleName.toLowerCase(Locale.ROOT).charAt(0)).append(simpleName, 1, simpleName.length()).append(' ');

        final QName qname = node.name().getNodeType();
        final QNameModule currentNamespace = qname.getModule();
        appendNamespace(sb, parentNamespace, currentNamespace);
        sb.append(qname.getLocalName()).append(' ');

        if (node instanceof NormalizedNodeContainer) {
            final NormalizedNodeContainer<?> container = (NormalizedNodeContainer<?>) node;
            sb.append("= {");

            final Iterator<? extends NormalizedNode> it = container.body().iterator();
            if (it.hasNext()) {
                final int childIndent = depth + 1;
                do {
                    sb.append('\n');
                    appendNode(sb, childIndent, currentNamespace, it.next());
                } while (it.hasNext());

                sb.append('\n');
                appendIndent(sb, depth);
            }
            sb.append('}');
        } else if (node instanceof ValueNode) {
            sb.append("= ");
            final Object value = node.body();
            if (value instanceof byte[]) {
                sb.append("(byte[])").append(Base64.getEncoder().encodeToString((byte[]) value));
            } else if (value instanceof String) {
                appendString(sb, (String) value);
            } else {
                sb.append(value);
            }
        } else if (node instanceof ForeignDataNode) {
            final ForeignDataNode<?> data = (ForeignDataNode<?>) node;
            final Object body = data.body();
            if (body instanceof PrettyTreeAware) {
                sb.append("= {\n");
                ((PrettyTreeAware) body).prettyTree().appendTo(sb, depth + 1);
                appendIndent(sb, depth);
                sb.append('}');
            } else {
                sb.append("= (").append(data.bodyObjectModel().getName()).append(')');
            }
        } else {
            throw new IllegalStateException("Unhandled node " + node);
        }
    }

    private static boolean appendNamespace(final StringBuilder sb, final QNameModule parent,
            final QNameModule current) {
        if (!current.equals(parent)) {
            sb.append('(').append(current.namespace());
            final var rev = current.revision();
            if (rev != null) {
                sb.append('@').append(rev);
            }
            sb.append(')');
            return true;
        }
        return false;
    }

    private static void appendString(final StringBuilder sb, final String str) {
        // TODO: do some escaping: '\r' '\n' '"' '\\' to make things even more zazzy
        sb.append('"').append(str).append('"');
    }
}
