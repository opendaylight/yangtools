/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class PatternStatementSupport
        extends AbstractStatementSupport<PatternExpression, PatternStatement, PatternEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(PatternStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addOptional(ErrorAppTagStatement.DEFINITION)
            .addOptional(ErrorMessageStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(PatternStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addOptional(ErrorAppTagStatement.DEFINITION)
            .addOptional(ErrorMessageStatement.DEFINITION)
            .addOptional(ModifierStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .build();

    private PatternStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(PatternStatement.DEFINITION, StatementPolicy.contextIndependent(), config, validator);
    }

    public static @NonNull PatternStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new PatternStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull PatternStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new PatternStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public PatternExpression parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final var pattern = RegexUtils.getJavaRegexFromXSD(value);
        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            throw new SourceException(ctx, e, "Pattern \"%s\" failed to compile", pattern);
        }
        return PatternExpression.of(value, pattern).intern();
    }

    @Override
    protected PatternStatement createDeclared(final BoundStmtCtx<PatternExpression> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createPattern(ctx.getArgument(), substatements);
    }

    @Override
    protected PatternStatement attachDeclarationReference(final PatternStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decoratePattern(stmt, reference);
    }

    @Override
    protected PatternEffectiveStatement createEffective(final Current<PatternExpression, PatternStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createPattern(stmt.declared(), substatements);
    }
}
