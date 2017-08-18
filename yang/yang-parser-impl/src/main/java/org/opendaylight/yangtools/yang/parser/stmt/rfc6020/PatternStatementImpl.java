/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternStatementImpl extends AbstractDeclaredStatement<PatternConstraint> implements PatternStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .PATTERN)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();
    private static final Logger LOG = LoggerFactory.getLogger(PatternStatementImpl.class);

    protected PatternStatementImpl(final StmtContext<PatternConstraint, PatternStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<PatternConstraint, PatternStatement, EffectiveStatement<PatternConstraint, PatternStatement>> {

        public Definition() {
            super(YangStmtMapping.PATTERN);
        }

        @Override
        public PatternConstraint parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            final String pattern = RegexUtils.getJavaRegexFromXSD(value);

            try {
                Pattern.compile(pattern);
            } catch (final PatternSyntaxException e) {
                LOG.debug("Pattern \"{}\" failed to compile at {}", pattern, ctx.getStatementSourceReference(), e);
                return null;
            }

            return new PatternConstraintEffectiveImpl(pattern, value, Optional.empty(), Optional.empty());
        }

        @Override
        public PatternStatement createDeclared(final StmtContext<PatternConstraint, PatternStatement, ?> ctx) {
            return new PatternStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<PatternConstraint, PatternStatement> createEffective(
                final StmtContext<PatternConstraint, PatternStatement, EffectiveStatement<PatternConstraint, PatternStatement>> ctx) {
            return new PatternEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public ErrorAppTagStatement getErrorAppTagStatement() {
        return firstDeclared(ErrorAppTagStatement.class);
    }

    @Override
    public ErrorMessageStatement getErrorMessageStatement() {
        return firstDeclared(ErrorMessageStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ModifierStatement getModifierStatement() {
        return firstDeclared(ModifierStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Nonnull
    @Override
    public PatternConstraint getValue() {
        return argument();
    }
}
