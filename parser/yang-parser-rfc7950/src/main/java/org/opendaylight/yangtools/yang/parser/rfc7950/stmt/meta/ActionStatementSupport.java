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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
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
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(ActionStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(GroupingStatement.DEFINITION)
            .addAny(IfFeatureStatement.DEFINITION)
            .addOptional(InputStatement.DEFINITION)
            .addOptional(OutputStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(StatusStatement.DEFINITION)
            .addAny(TypedefStatement.DEFINITION)
            .build();

    public ActionStatementSupport(final YangParserConfiguration config) {
        super(ActionStatement.DEFINITION, uninstantiatedPolicy(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, ActionStatement, ActionEffectiveStatement> stmt) {
        final var argument = stmt.getArgument();
        final var parent = stmt.getParentContext();
        if (parent != null) {
            if (parent.inStructure()) {
                throw new SourceException(stmt, "Action %s is defined within another structure", argument);
            }
            if (parent.producesDeclared(CaseStatement.class)) {
                throw new SourceException(stmt, "Action %s is defined within a case statement", argument);
            }
            if (parent.getParentContext() == null) {
                throw new SourceException(stmt, "Action %s is defined at the top level of a source file", argument);
            }
        }

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
