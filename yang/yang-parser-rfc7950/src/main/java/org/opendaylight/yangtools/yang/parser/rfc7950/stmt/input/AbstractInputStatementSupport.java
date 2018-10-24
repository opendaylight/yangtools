/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractInputStatementSupport
        extends AbstractQNameStatementSupport<InputStatement, EffectiveStatement<QName, InputStatement>> {
    AbstractInputStatementSupport() {
        super(YangStmtMapping.INPUT);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return YangConstants.operationInputQName(StmtContextUtils.getRootModuleQName(ctx));
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, InputStatement,
            EffectiveStatement<QName, InputStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public final InputStatement createDeclared(final StmtContext<QName, InputStatement, ?> ctx) {
        return new InputStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, InputStatement> createEffective(
            final StmtContext<QName, InputStatement, EffectiveStatement<QName, InputStatement>> ctx) {
        return new InputEffectiveStatementImpl(ctx);
    }
}