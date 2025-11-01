/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public final class YangNamespaceContextNamespace {
    private static final @NonNull ParserNamespace<StmtContext<?, ?, ?>, YangNamespaceContext> INSTANCE =
        new ParserNamespace<>("yangNamespaceContext");
    public static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.global(INSTANCE);

    private YangNamespaceContextNamespace() {
        // Hidden on purpose
    }

    public static @NonNull YangNamespaceContext computeIfAbsent(final StmtContext<?, ?, ?> ctx) {
        final var root = ctx.getRoot();
        YangNamespaceContext ret = ctx.namespaceItem(INSTANCE, root);
        if (ret == null) {
            if (!(ctx instanceof Mutable<?, ?, ?> mutable)) {
                throw new VerifyException("Cannot populate namespace context to " + ctx);
            }
            ret = new StmtNamespaceContext(root);
            mutable.addToNs(INSTANCE, root, ret);
        }
        return ret;
    }
}
