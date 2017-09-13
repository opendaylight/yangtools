/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

public final class DefaultEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, DefaultStatement> {
    private final QNameModule module;
    public DefaultEffectiveStatementImpl(final StmtContext<String, DefaultStatement, ?> ctx) {
        super(ctx);
        StmtContext<?, ?, ?> original = ctx;
        while (original.getOriginalCtx().isPresent()) {
            original = original.getOriginalCtx().get();
        }
        module = original.getRoot().getFromNamespace(ModuleCtxToModuleQName.class, original.getRoot());
    }

    public QNameModule getModule() {
        return module;
    }
}
