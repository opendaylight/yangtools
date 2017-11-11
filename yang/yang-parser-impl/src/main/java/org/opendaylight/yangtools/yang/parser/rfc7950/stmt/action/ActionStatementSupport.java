/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementDefinitionContext;

public final class ActionStatementSupport
        extends AbstractQNameStatementSupport<ActionStatement, EffectiveStatement<QName, ActionStatement>> {
    private static final Set<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(YangStmtMapping.NOTIFICATION,
            YangStmtMapping.RPC, YangStmtMapping.ACTION);
    private static final StatementSupport<?, ?, ?> IMPLICIT_INPUT = new InputStatementRFC7950Support();
    private static final StatementSupport<?, ?, ?> IMPLICIT_OUTPUT = new OutputStatementRFC7950Support();

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ACTION)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.INPUT)
        .addOptional(YangStmtMapping.OUTPUT)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .build();

    public ActionStatementSupport() {
        super(YangStmtMapping.ACTION);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public void onStatementAdded(
            final StmtContext.Mutable<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> stmt) {
        stmt.getParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public ActionStatement createDeclared(
            final StmtContext<QName, ActionStatement, ?> ctx) {
        return new ActionStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, ActionStatement> createEffective(
            final StmtContext<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> ctx) {
        SourceException.throwIf(StmtContextUtils.hasAncestorOfType(ctx, ILLEGAL_PARENTS),
                ctx.getStatementSourceReference(),
                "Action %s is defined within a notification, rpc or another action", ctx.getStatementArgument());
        SourceException.throwIf(!StmtContextUtils.hasAncestorOfTypeWithChildOfType(ctx, YangStmtMapping.LIST,
                YangStmtMapping.KEY), ctx.getStatementSourceReference(),
                "Action %s is defined within a list that has no key statement", ctx.getStatementArgument());
        SourceException.throwIf(StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.CASE),
                ctx.getStatementSourceReference(), "Action %s is defined within a case statement",
                ctx.getStatementArgument());
        SourceException.throwIf(StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.MODULE),
                ctx.getStatementSourceReference(), "Action %s is defined at the top level of a module",
                ctx.getStatementArgument());
        return new ActionEffectiveStatementImpl(ctx);
    }

    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<QName, ActionStatement,
            EffectiveStatement<QName, ActionStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
            addImplicitStatement((StatementContextBase<?, ?, ?>) stmt, IMPLICIT_INPUT);
        }

        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
            addImplicitStatement((StatementContextBase<?, ?, ?>) stmt, IMPLICIT_OUTPUT);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    private static void addImplicitStatement(final StatementContextBase<?, ?, ?> parent,
        final StatementSupport<?, ?, ?> statementToAdd) {
        final StatementDefinitionContext<?, ?, ?> stmtDefCtx = new StatementDefinitionContext<>(statementToAdd);

        parent.createSubstatement(parent.declaredSubstatements().size(), stmtDefCtx,
                ImplicitSubstatement.of(parent.getStatementSourceReference()), null);
    }
}