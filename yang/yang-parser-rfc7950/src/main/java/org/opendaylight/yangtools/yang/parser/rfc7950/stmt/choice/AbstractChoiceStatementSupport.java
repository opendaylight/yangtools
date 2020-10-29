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
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractChoiceStatementSupport
        extends BaseSchemaTreeStatementSupport<ChoiceStatement, ChoiceEffectiveStatement>
        implements ImplicitParentAwareStatementSupport {
    AbstractChoiceStatementSupport() {
        super(YangStmtMapping.CHOICE);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        return YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmtDef) ? Optional.of(implictCase())
                : Optional.empty();
    }

    @Override
    protected final ChoiceStatement createDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularChoiceStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final ChoiceStatement createEmptyDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx) {
        return new EmptyChoiceStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final ChoiceEffectiveStatement createEffective(
            final StmtContext<QName, ChoiceStatement, ChoiceEffectiveStatement> ctx,
            final ChoiceStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final String defaultArg = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        final CaseSchemaNode defaultCase;
        if (defaultArg != null) {
            final QName qname;
            try {
                qname = QName.create(ctx.coerceStatementArgument(), defaultArg);
            } catch (IllegalArgumentException e) {
                throw new SourceException(ctx.getStatementSourceReference(), "Default statement has invalid name '%s'",
                    defaultArg, e);
            }

            // FIXME: this does not work with submodules, as they are
            defaultCase = InferenceException.throwIfNull(findCase(qname, substatements),
                ctx.getStatementSourceReference(), "Default statement refers to missing case %s", qname);
        } else {
            defaultCase = null;
        }

        final int flags = new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();

        return new ChoiceEffectiveStatementImpl(declared, ctx, substatements, flags, defaultCase,
            (ChoiceSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null));
    }

    @Override
    protected final ChoiceEffectiveStatement createEmptyEffective(
            final StmtContext<QName, ChoiceStatement, ChoiceEffectiveStatement> ctx, final ChoiceStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
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