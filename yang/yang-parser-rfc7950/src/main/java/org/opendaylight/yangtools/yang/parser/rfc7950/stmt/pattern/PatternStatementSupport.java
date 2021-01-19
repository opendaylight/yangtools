/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class PatternStatementSupport
        extends AbstractStatementSupport<PatternExpression, PatternStatement, PatternEffectiveStatement> {
    private static final @NonNull PatternStatementSupport RFC6020_INSTANCE = new PatternStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.PATTERN)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.REFERENCE)
            .build());
    private static final @NonNull PatternStatementSupport RFC7950_INSTANCE = new PatternStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.PATTERN)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.MODIFIER)
            .addOptional(YangStmtMapping.REFERENCE)
            .build());

    private final SubstatementValidator validator;

    private PatternStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.PATTERN, StatementPolicy.contextIndependent());
        this.validator = requireNonNull(validator);
    }

    public static @NonNull PatternStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull PatternStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public PatternExpression parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final String pattern = RegexUtils.getJavaRegexFromXSD(value);
        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            throw new SourceException(ctx, e, "Pattern \"%s\" failed to compile", pattern);
        }
        return PatternExpression.of(value, pattern);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected PatternStatement createDeclared(final StmtContext<PatternExpression, PatternStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPatternStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected PatternStatement createEmptyDeclared(
            final StmtContext<PatternExpression, PatternStatement, ?> ctx) {
        return new EmptyPatternStatement(ctx.getArgument());
    }

    @Override
    protected PatternEffectiveStatement createEffective(final Current<PatternExpression, PatternStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPatternEffectiveStatement(stmt.declared())
            : new RegularPatternEffectiveStatement(stmt.declared(), substatements);
    }
}
