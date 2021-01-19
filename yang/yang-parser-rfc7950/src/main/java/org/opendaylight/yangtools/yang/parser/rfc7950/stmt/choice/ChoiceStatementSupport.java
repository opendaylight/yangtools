/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_.CaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ChoiceStatementSupport
        extends BaseSchemaTreeStatementSupport<ChoiceStatement, ChoiceEffectiveStatement>
        implements ImplicitParentAwareStatementSupport {
    private static final @NonNull ChoiceStatementSupport RFC6020_INSTANCE = new ChoiceStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CASE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.WHEN)
            .build(),
        CaseStatementSupport.rfc6020Instance());

    private static final @NonNull ChoiceStatementSupport RFC7950_INSTANCE = new ChoiceStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CASE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.WHEN)
            .build(),
        CaseStatementSupport.rfc7950Instance());

    private final SubstatementValidator validator;
    private final CaseStatementSupport implicitCase;

    private ChoiceStatementSupport(final SubstatementValidator validator, final CaseStatementSupport implicitCase) {
        super(YangStmtMapping.CHOICE, StatementPolicy.legacyDeclaredCopy());
        this.validator = requireNonNull(validator);
        this.implicitCase = requireNonNull(implicitCase);
    }

    public static @NonNull ChoiceStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull ChoiceStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        return YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmtDef) ? Optional.of(implicitCase)
            : Optional.empty();
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected ChoiceStatement createDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularChoiceStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected ChoiceStatement createEmptyDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx) {
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
            defaultCase = InferenceException.throwIfNull(findCase(qname, substatements), stmt,
                "Default statement refers to missing case %s", qname);
        } else {
            defaultCase = null;
        }

        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();
        try {
            return new ChoiceEffectiveStatementImpl(stmt.declared(), substatements, flags, stmt.wrapSchemaPath(),
                defaultCase, (ChoiceSchemaNode) stmt.original());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

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