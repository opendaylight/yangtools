/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternStatementImpl extends AbstractDeclaredStatement<PatternConstraint> implements PatternStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .PATTERN)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addOptional(Rfc6020Mapping.ERROR_APP_TAG)
            .addOptional(Rfc6020Mapping.ERROR_MESSAGE)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .build();
    private static final Logger LOG = LoggerFactory.getLogger(PatternStatementImpl.class);

    protected PatternStatementImpl(final StmtContext<PatternConstraint, PatternStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<PatternConstraint, PatternStatement, EffectiveStatement<PatternConstraint, PatternStatement>> {

        public Definition() {
            super(Rfc6020Mapping.PATTERN);
        }

        @Override
        public PatternConstraint parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            final String pattern = getJavaRegexFromXSD(value);

            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                LOG.debug("Pattern \"{}\" failed to compile at {}", pattern, ctx.getStatementSourceReference(), e);
                return null;
            }

            return new PatternConstraintEffectiveImpl(pattern, value, Optional.absent(), Optional.absent());
        }

        static String getJavaRegexFromXSD(final String xsdRegex) {
            return "^" + Utils.fixUnicodeScriptPattern(escapeChars(xsdRegex)) + '$';
        }

        /*
         * As both '^' and '$' are special anchor characters in java regular
         * expressions which are implicitly present in XSD regular expressions,
         * we need to escape them in case they are not defined as part of
         * character ranges i.e. inside regular square brackets.
         */
        private static String escapeChars(final String regex) {
            final StringBuilder result = new StringBuilder(regex.length());
            int bracket = 0;
            boolean escape = false;
            for (int i = 0; i < regex.length(); i++) {
                final char ch = regex.charAt(i);
                switch (ch) {
                case '[':
                    if (!escape) {
                        bracket++;
                    }
                    escape = false;
                    result.append(ch);
                    break;
                case ']':
                    if (!escape) {
                        bracket--;
                    }
                    escape = false;
                    result.append(ch);
                    break;
                case '\\':
                    escape = !escape;
                    result.append(ch);
                    break;
                case '^':
                case '$':
                    if (bracket == 0) {
                        result.append('\\');
                    }
                    escape = false;
                    result.append(ch);
                    break;
                default:
                    escape = false;
                    result.append(ch);
                }
            }
            return result.toString();
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
        public void onFullDefinitionDeclared(final StmtContext.Mutable<PatternConstraint, PatternStatement,
                EffectiveStatement<PatternConstraint, PatternStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
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
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public PatternConstraint getValue() {
        return argument();
    }

}
