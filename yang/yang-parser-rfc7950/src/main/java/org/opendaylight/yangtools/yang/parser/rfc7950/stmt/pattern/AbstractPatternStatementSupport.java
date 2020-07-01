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
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractPatternStatementSupport
        extends BaseStatementSupport<PatternConstraint, PatternStatement, PatternEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPatternStatementSupport.class);

    AbstractPatternStatementSupport() {
        super(YangStmtMapping.PATTERN, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    @Override
    public final PatternConstraint parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final String pattern = RegexUtils.getJavaRegexFromXSD(value);

        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            LOG.debug("Pattern \"{}\" failed to compile at {}", pattern, ctx.getStatementSourceReference(), e);
            return null;
        }

        return new PatternConstraintImpl(pattern, value);
    }

    @Override
    protected final PatternStatement createDeclared(final StmtContext<PatternConstraint, PatternStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPatternStatement(ctx, substatements);
    }

    @Override
    protected final PatternStatement createEmptyDeclared(
            final StmtContext<PatternConstraint, PatternStatement, ?> ctx) {
        return new EmptyPatternStatement(ctx);
    }

    @Override
    protected final PatternEffectiveStatement createEffective(
            final StmtContext<PatternConstraint, PatternStatement, PatternEffectiveStatement> ctx,
            final PatternStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final String description = findFirstArgument(substatements, DescriptionEffectiveStatement.class, null);
        final String reference = findFirstArgument(substatements, ReferenceEffectiveStatement.class, null);
        final String errorAppTag = findFirstArgument(substatements, ErrorAppTagEffectiveStatement.class, null);
        final String errorMessage = findFirstArgument(substatements, ErrorMessageEffectiveStatement.class, null);
        final ModifierKind modifier = findFirstArgument(substatements, ModifierEffectiveStatement.class, null);

        if (description == null && reference == null && errorAppTag == null && errorMessage == null
                && modifier == null) {
            // No customization, just use declared statement for the actual value
            return new SimplePatternEffectiveStatement(declared, substatements);
        }

        return new RegularPatternEffectiveStatement(declared, new PatternConstraintImpl(declared.argument(),
            description, reference, errorAppTag, errorMessage, modifier), substatements);
    }

    @Override
    protected final PatternEffectiveStatement createEmptyEffective(
            final StmtContext<PatternConstraint, PatternStatement, PatternEffectiveStatement> ctx,
            final PatternStatement declared) {
        return new EmptyPatternEffectiveStatement(declared);
    }
}
