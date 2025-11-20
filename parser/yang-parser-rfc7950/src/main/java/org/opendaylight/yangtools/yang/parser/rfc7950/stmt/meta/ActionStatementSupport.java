/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ActionStatementSupport
        extends AbstractOperationStatementSupport<ActionStatement, ActionEffectiveStatement> {
    private static final ImmutableSet<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(
            YangStmtMapping.NOTIFICATION, YangStmtMapping.RPC, YangStmtMapping.ACTION);

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.ACTION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.INPUT)
            .addOptional(YangStmtMapping.OUTPUT)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .build();

    public ActionStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.ACTION, uninstantiatedPolicy(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, ActionStatement, ActionEffectiveStatement> stmt) {
        final QName argument = stmt.getArgument();
        SourceException.throwIf(StmtContextUtils.hasAncestorOfType(stmt, ILLEGAL_PARENTS), stmt,
            "Action %s is defined within a notification, rpc or another action", argument);
        SourceException.throwIf(StmtContextUtils.hasParentOfType(stmt, YangStmtMapping.CASE), stmt,
            "Action %s is defined within a case statement", argument);
        SourceException.throwIf(StmtContextUtils.hasParentOfType(stmt, YangStmtMapping.MODULE), stmt,
            "Action %s is defined at the top level of a module", argument);
        StmtContextUtils.validateNoKeylessListAncestorOf(stmt, "Action");

        super.onStatementAdded(stmt);
    }

    @Override
    protected ActionStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createAction(ctx.getArgument(), substatements);
    }

    @Override
    protected ActionStatement attachDeclarationReference(final ActionStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateAction(stmt, reference);
    }

    @Override
    ActionEffectiveStatement createEffectiveImpl(final Current<QName, ActionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createAction(stmt.declared(), stmt.getArgument(),
            EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), substatements), substatements);
    }

    @Override
    public ActionEffectiveStatement copyEffective(final Current<QName, ActionStatement> stmt,
            final ActionEffectiveStatement original) {
        return EffectiveStatements.copyAction(original, stmt.getArgument(),
            EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()));
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final ActionEffectiveStatement stmt) {
        verify(stmt instanceof ActionDefinition, "Unexpected statement %s", stmt);
        final var schema = (ActionDefinition) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(),
            EffectiveStmtUtils.historyAndStatusFlags(schema));
    }
}
