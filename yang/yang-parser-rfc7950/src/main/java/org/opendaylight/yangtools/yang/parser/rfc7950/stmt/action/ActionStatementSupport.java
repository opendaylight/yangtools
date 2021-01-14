/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class ActionStatementSupport extends
        BaseSchemaTreeStatementSupport<ActionStatement, ActionEffectiveStatement> {

    private static final ImmutableSet<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(
            YangStmtMapping.NOTIFICATION, YangStmtMapping.RPC, YangStmtMapping.ACTION);

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
    private static final ActionStatementSupport INSTANCE = new ActionStatementSupport();

    private ActionStatementSupport() {
        super(YangStmtMapping.ACTION, CopyPolicy.DECLARED_COPY);
    }

    public static ActionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public void onStatementAdded(final Mutable<QName, ActionStatement, ActionEffectiveStatement> stmt) {
        final QName argument = stmt.getArgument();
        SourceException.throwIf(StmtContextUtils.hasAncestorOfType(stmt, ILLEGAL_PARENTS), stmt,
            "Action %s is defined within a notification, rpc or another action", argument);
        SourceException.throwIf(StmtContextUtils.hasParentOfType(stmt, YangStmtMapping.CASE), stmt,
            "Action %s is defined within a case statement", argument);
        SourceException.throwIf(StmtContextUtils.hasParentOfType(stmt, YangStmtMapping.MODULE), stmt,
            "Action %s is defined at the top level of a module", stmt.getArgument());
        StmtContextUtils.validateNoKeylessListAncestorOf(stmt, "Action");

        super.onStatementAdded(stmt);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, ActionStatement, ActionEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        verify(stmt instanceof StatementContextBase);
        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitSubstatement(
                InputStatementSupport.rfc7950Instance(), null);
        }
        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitSubstatement(
                OutputStatementSupport.rfc7950Instance(), null);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ActionStatement createDeclared(final StmtContext<QName, ActionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularActionStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected ActionStatement createEmptyDeclared(final StmtContext<QName, ActionStatement, ?> ctx) {
        return new EmptyActionStatement(ctx.getArgument());
    }

    @Override
    protected ActionEffectiveStatement createEffective(final Current<QName, ActionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StatementSourceReference ref = stmt.sourceReference();
        verify(!substatements.isEmpty(), "Missing implicit input/output statements at %s", ref);

        try {
            return new ActionEffectiveStatementImpl(stmt.declared(), stmt.wrapSchemaPath(),
                historyAndStatusFlags(stmt.history(), substatements), substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }
}
