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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractPatternStatementSupport
        extends BaseStatementSupport<PatternExpression, PatternStatement, PatternEffectiveStatement> {
    AbstractPatternStatementSupport() {
        super(YangStmtMapping.PATTERN, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    @Override
    public final PatternExpression parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final String pattern = RegexUtils.getJavaRegexFromXSD(value);
        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e,
                "Pattern \"%s\" failed to compile", pattern);
        }
        return PatternExpression.of(value, pattern);
    }

    @Override
    protected final PatternStatement createDeclared(final StmtContext<PatternExpression, PatternStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPatternStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final PatternStatement createEmptyDeclared(
            final StmtContext<PatternExpression, PatternStatement, ?> ctx) {
        return new EmptyPatternStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final PatternEffectiveStatement createEffective(
            final StmtContext<PatternExpression, PatternStatement, PatternEffectiveStatement> ctx,
            final PatternStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularPatternEffectiveStatement(declared, substatements);
    }

    @Override
    protected final PatternEffectiveStatement createEmptyEffective(
            final StmtContext<PatternExpression, PatternStatement, PatternEffectiveStatement> ctx,
            final PatternStatement declared) {
        return new EmptyPatternEffectiveStatement(declared);
    }
}
