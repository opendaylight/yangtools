/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractPatternStatementSupport extends AbstractStatementSupport<PatternConstraint, PatternStatement,
        EffectiveStatement<PatternConstraint, PatternStatement>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPatternStatementSupport.class);

    AbstractPatternStatementSupport() {
        super(YangStmtMapping.PATTERN);
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

        return new PatternConstraintImpl(pattern, value, Optional.empty(), Optional.empty());
    }

    @Override
    public final PatternStatement createDeclared(final StmtContext<PatternConstraint, PatternStatement, ?> ctx) {
        return new PatternStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<PatternConstraint, PatternStatement> createEffective(
            final StmtContext<PatternConstraint, PatternStatement,
            EffectiveStatement<PatternConstraint, PatternStatement>> ctx) {
        return new PatternEffectiveStatementImpl(ctx);
    }
}