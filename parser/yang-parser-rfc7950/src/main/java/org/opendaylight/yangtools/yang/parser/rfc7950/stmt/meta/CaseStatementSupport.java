/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
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
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.UndeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent.EffectiveConfig;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
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
    protected CaseStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createCase(ctx.getArgument(), substatements);
    }

    @Override
    protected CaseStatement attachDeclarationReference(final CaseStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateCase(stmt, reference);
    }

    @Override
    protected CaseEffectiveStatement createEffective(final Current<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createCase(stmt.declared(), stmt.getArgument(),
                computeFlags(stmt, substatements), substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public CaseEffectiveStatement copyEffective(final Current<QName, CaseStatement> stmt,
            final CaseEffectiveStatement original) {
        return EffectiveStatements.copyCase(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()));
    }

    @Override
    protected CaseEffectiveStatement createUndeclaredEffective(final UndeclaredCurrent<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return UndeclaredStatements.createCase(stmt.getArgument(), computeFlags(stmt, substatements),
                substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final CaseEffectiveStatement stmt) {
        if (!(stmt instanceof CaseSchemaNode schema)) {
            throw new VerifyException("Unexpected statement " + stmt);
        }
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(), new FlagsBuilder()
            .setHistory(schema)
            .setStatus(schema.getStatus())
            .setConfiguration(schema.effectiveConfig().orElse(null))
            .toFlags());
    }

    private static int computeFlags(final Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        final Boolean config;
        final EffectiveConfig effective = stmt.effectiveConfig();
        config = switch (effective) {
            case FALSE -> Boolean.FALSE;
            case IGNORED -> null;
            case TRUE -> {
                final Boolean sub = substatementEffectiveConfig(substatements);
                yield sub != null ? sub : Boolean.TRUE;
            }
            case UNDETERMINED -> substatementEffectiveConfig(substatements);
            default -> throw new IllegalStateException("Unhandled effective config " + effective);
        };

        return new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(config)
                .toFlags();
    }

    private static @Nullable Boolean substatementEffectiveConfig(
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        for (var stmt : substatements) {
            if (stmt instanceof DataSchemaNode dsn) {
                final var opt = dsn.effectiveConfig();
                if (opt.isPresent()) {
                    return opt.orElseThrow();
                }
            }
        }
        return null;
    }
}
