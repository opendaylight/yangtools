/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

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
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PatternEffectiveStatementImpl;

import com.google.common.base.Optional;

public class PatternStatementImpl extends AbstractDeclaredStatement<PatternConstraint> implements PatternStatement {

    protected PatternStatementImpl(StmtContext<PatternConstraint, PatternStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<PatternConstraint, PatternStatement, EffectiveStatement<PatternConstraint, PatternStatement>> {

        public Definition() {
            super(Rfc6020Mapping.PATTERN);
        }

        @Override
        public PatternConstraint parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {

            try {
                Pattern.compile(value);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(String.format(
                        "Pattern %s has not format of a valid regular expression", value), e);
            }

            return new PatternConstraintEffectiveImpl(value, Optional.of(""), Optional.of(""));
        }

        @Override
        public PatternStatement createDeclared(StmtContext<PatternConstraint, PatternStatement, ?> ctx) {
            return new PatternStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<PatternConstraint, PatternStatement> createEffective(
                StmtContext<PatternConstraint, PatternStatement, EffectiveStatement<PatternConstraint, PatternStatement>> ctx) {
            return new PatternEffectiveStatementImpl(ctx);
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
