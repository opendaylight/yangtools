/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractChoiceStatementSupport
        extends BaseSchemaTreeStatementSupport<ChoiceStatement, ChoiceEffectiveStatement>
        implements ImplicitParentAwareStatementSupport {
    AbstractChoiceStatementSupport() {
        super(YangStmtMapping.CHOICE);
    }

    @Override
    public final Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        return YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmtDef) ? Optional.of(implictCase())
                : Optional.empty();
    }

    @Override
    protected final ChoiceStatement createDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularChoiceStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected final ChoiceStatement createEmptyDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx) {
        return new EmptyChoiceStatement(ctx.getArgument());
    }

    @Override
    protected ChoiceEffectiveStatement createEffective(final Current<QName, ChoiceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final String defaultArg = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        final CaseSchemaNode defaultCase;
        if (defaultArg != null) {
            final QName qname;
            try {
                qname = QName.create(stmt.getArgument(), defaultArg);
            } catch (IllegalArgumentException e) {
                throw new SourceException(stmt, e, "Default statement has invalid name '%s'", defaultArg);
            }

            // FIXME: this does not work with submodules, as they are
            defaultCase = InferenceException.throwIfNull(findCase(qname, substatements),
                    stmt.sourceReference(), "Default statement refers to missing case %s", qname);
        } else {
            defaultCase = null;
        }

        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();
        try {
            return new ChoiceEffectiveStatementImpl(stmt.declared(), substatements, flags, stmt.getSchemaPath(),
                defaultCase, (ChoiceSchemaNode) stmt.original());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt.sourceReference(), e);
        }
    }

    abstract StatementSupport<?, ?, ?> implictCase();

    private static CaseSchemaNode findCase(final QName qname,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        for (final EffectiveStatement<?, ?> effectiveStatement : substatements) {
            if (effectiveStatement instanceof CaseSchemaNode) {
                final CaseSchemaNode choiceCaseNode = (CaseSchemaNode) effectiveStatement;
                if (qname.equals(choiceCaseNode.getQName())) {
                    return choiceCaseNode;
                }
            }
        }

        return null;
    }
}