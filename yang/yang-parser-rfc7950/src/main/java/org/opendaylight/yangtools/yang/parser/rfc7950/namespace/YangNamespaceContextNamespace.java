/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.GlobalIdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

@Beta
public interface YangNamespaceContextNamespace
        extends GlobalIdentifierNamespace<StmtContext<?, ?, ?>, YangNamespaceContext> {
    NamespaceBehaviour<StmtContext<?, ?, ?>, YangNamespaceContext, @NonNull YangNamespaceContextNamespace> BEHAVIOUR =
            NamespaceBehaviour.globalOf(YangNamespaceContextNamespace.class);

    static @NonNull YangNamespaceContext computeIfAbsent(final StmtContext<?, ?, ?> ctx) {
        final StmtContext<?, ?, ?> root = ctx.getRoot();
        YangNamespaceContext ret = ctx.getFromNamespace(YangNamespaceContextNamespace.class, root);
        if (ret == null) {
            verify(ctx instanceof Mutable, "Cannot populate namespace context to %s", ctx);
            ret = new StmtNamespaceContext(root);
            ((Mutable<?, ?, ?>)ctx).addToNs(YangNamespaceContextNamespace.class, root, ret);
        }
        return ret;
    }
}
