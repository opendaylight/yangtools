/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.ImplicitStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent.EffectiveConfig;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class CaseStatementSupport
        extends AbstractImplicitStatementSupport<CaseStatement, CaseEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.CASE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.CASE)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    private CaseStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.CASE, instantiatedPolicy(), config, validator);
    }

    public static @NonNull CaseStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new CaseStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull CaseStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new CaseStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected CaseStatement createDeclared(final StmtContext<QName, CaseStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementOrigin origin = ctx.origin();
        switch (origin) {
            case CONTEXT:
                return ImplicitStatements.createCase(ctx.getArgument(), substatements);
            case DECLARATION:
                return DeclaredStatements.createCase(ctx.getArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement origin " + origin);
        }
    }

    @Override
    protected CaseStatement attachDeclarationReference(final CaseStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateCase(stmt, reference);
    }

    @Override
    protected CaseEffectiveStatement copyDeclaredEffective(final Current<QName, CaseStatement> stmt,
            final CaseEffectiveStatement original) {
        return EffectiveStatements.copyCase(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()));
    }

    @Override
    protected CaseEffectiveStatement copyUndeclaredEffective(final Current<QName, CaseStatement> stmt,
            final CaseEffectiveStatement original) {
        return EffectiveStatements.copyCase(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()));
    }

    @Override
    protected CaseEffectiveStatement createDeclaredEffective(final Current<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createCase(stmt.declared(), stmt.getArgument(),
                computeFlags(stmt, substatements), substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    protected CaseEffectiveStatement createUndeclaredEffective(final Current<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createCase(stmt.getArgument(), computeFlags(stmt, substatements), substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final CaseEffectiveStatement stmt) {
        verify(stmt instanceof CaseSchemaNode, "Unexpected statement %s", stmt);
        final var schema = (CaseSchemaNode) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(), new FlagsBuilder()
            .setStatus(schema.getStatus())
            .setConfiguration(schema.effectiveConfig().orElse(null))
            .toFlags());
    }

    private static int computeFlags(final Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        final Boolean config;
        final EffectiveConfig effective = stmt.effectiveConfig();
        switch (effective) {
            case FALSE:
                config = Boolean.FALSE;
                break;
            case IGNORED:
                config = null;
                break;
            case TRUE:
                final Boolean sub = substatementEffectiveConfig(substatements);
                config = sub != null ? sub : Boolean.TRUE;
                break;
            case UNDETERMINED:
                config = substatementEffectiveConfig(substatements);
                break;
            default:
                throw new IllegalStateException("Unhandled effective config " + effective);
        }

        return new FlagsBuilder()
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(config)
            .toFlags();
    }

    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Internal use tagged with @Nullable")
    private static @Nullable Boolean substatementEffectiveConfig(
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof DataSchemaNode) {
                final Optional<Boolean> opt = ((DataSchemaNode) stmt).effectiveConfig();
                if (opt.isPresent()) {
                    return opt.orElseThrow();
                }
            }
        }
        return null;
    }
}
