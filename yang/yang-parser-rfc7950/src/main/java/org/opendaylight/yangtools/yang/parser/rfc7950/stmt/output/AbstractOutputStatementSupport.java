/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractOutputStatementSupport extends
        AbstractQNameStatementSupport<OutputStatement, EffectiveStatement<QName, OutputStatement>> {
    AbstractOutputStatementSupport() {
        super(YangStmtMapping.OUTPUT);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return YangConstants.operationOutputQName(StmtContextUtils.getRootModuleQName(ctx));
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, OutputStatement,
            EffectiveStatement<QName, OutputStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public final OutputStatement createDeclared(final StmtContext<QName, OutputStatement, ?> ctx) {
        return new OutputStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, OutputStatement> createEffective(
            final StmtContext<QName, OutputStatement, EffectiveStatement<QName, OutputStatement>> ctx) {
        return new OutputEffectiveStatementImpl(ctx);
    }
}