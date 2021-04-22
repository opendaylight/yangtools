/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ChoiceStatementSupport
        extends AbstractSchemaTreeStatementSupport<ChoiceStatement, ChoiceEffectiveStatement>
        implements ImplicitParentAwareStatementSupport {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.CHOICE)
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
            .build();

    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.CHOICE)
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
            .build();

    private final SubstatementValidator validator;
    private final CaseStatementSupport implicitCase;

    private ChoiceStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator,
            final CaseStatementSupport implicitCase) {
        super(YangStmtMapping.CHOICE, instantiatedPolicy(), config);
        this.validator = requireNonNull(validator);
        this.implicitCase = requireNonNull(implicitCase);
    }

    public static @NonNull ChoiceStatementSupport rfc6020Instance(final YangParserConfiguration config,
            final CaseStatementSupport implicitCase) {
        return new ChoiceStatementSupport(config, RFC6020_VALIDATOR, implicitCase);
    }

    public static @NonNull ChoiceStatementSupport rfc7950Instance(final YangParserConfiguration config,
            final CaseStatementSupport implicitCase) {
        return new ChoiceStatementSupport(config, RFC7950_VALIDATOR, implicitCase);
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
        return DeclaredStatements.createChoice(ctx.getArgument(), substatements);
    }

    @Override
    protected ChoiceStatement attachDeclarationReference(final ChoiceStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateChoice(stmt, reference);
    }

    @Override
    public ChoiceEffectiveStatement copyEffective(final Current<QName, ChoiceStatement> stmt,
            final ChoiceEffectiveStatement original) {
        return EffectiveStatements.copyChoice(original, stmt.effectivePath(),
            computeFlags(stmt, original.effectiveSubstatements()), stmt.original(ChoiceSchemaNode.class));
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

        try {
            return EffectiveStatements.createChoice(stmt.declared(), stmt.effectivePath(),
                computeFlags(stmt, substatements), substatements, defaultCase, stmt.original(ChoiceSchemaNode.class));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static int computeFlags(final Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
            .toFlags();
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