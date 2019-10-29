/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ModuleQNameToPrefix;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * A {@link NamespaceContext} implementation based on the set of imports and local module namespace.
 */
// TODO: this is a useful utility, so it may be useful to expose it either in this package, or yang.parser.spi.source.
final class StmtNamespaceContext implements YangNamespaceContext {
    // FIXME: add serialization barrier
    private static final long serialVersionUID = 1L;

    private final StmtContext<?, ?, ?> ctx;

    StmtNamespaceContext(final StmtContext<?, ?, ?> ctx) {
        this.ctx = requireNonNull(ctx);
    }

    @Override
    public Optional<String> findPrefixForNamespace(final QNameModule namespace) {
        return Optional.ofNullable(ctx.getFromNamespace(ModuleQNameToPrefix.class, namespace));
    }

    @Override
    public Optional<QNameModule> findNamespaceForPrefix(final String prefix) {
        // TODO: perform caching?
        return Optional.ofNullable(StmtContextUtils.getModuleQNameByPrefix(ctx, prefix));
    }
}
