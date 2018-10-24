/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

abstract class AbstractRpcStatementSupport
        extends AbstractQNameStatementSupport<RpcStatement, EffectiveStatement<QName, RpcStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.RPC)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.INPUT)
        .addOptional(YangStmtMapping.OUTPUT)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .build();

    AbstractRpcStatementSupport() {
        super(YangStmtMapping.RPC);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final void onStatementAdded(
            final Mutable<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public final RpcStatement createDeclared(final StmtContext<QName, RpcStatement, ?> ctx) {
        return new RpcStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, RpcStatement> createEffective(
            final StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> ctx) {
        return new RpcEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitStatement(implictInput());
        }

        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitStatement(implictOutput());
        }
    }


    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    abstract StatementSupport<?, ?, ?> implictInput();

    abstract StatementSupport<?, ?, ?> implictOutput();
}
