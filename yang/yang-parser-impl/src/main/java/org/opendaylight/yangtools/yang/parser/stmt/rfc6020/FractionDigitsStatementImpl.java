/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;

import com.google.common.collect.Range;

public class FractionDigitsStatementImpl extends AbstractDeclaredStatement<Integer> implements FractionDigitsStatement {

    private static final Range<Integer> FRAC_DIGITS_ALLOWED = Range.closed(1, 18);

    protected FractionDigitsStatementImpl(StmtContext<Integer, FractionDigitsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Integer, FractionDigitsStatement, EffectiveStatement<Integer, FractionDigitsStatement>> {

        public Definition() {
            super(Rfc6020Mapping.FRACTION_DIGITS);
        }

        @Override
        public Integer parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {

            int fractionDigits;

            try {
                fractionDigits = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("%s is not valid fraction-digits integer argument",
                        value), e);
            }

            if (!FRAC_DIGITS_ALLOWED.contains(fractionDigits)) {
                throw new IllegalArgumentException(String.format("fraction-digits argument should be integer within %s",
                        FRAC_DIGITS_ALLOWED));
            }

            return fractionDigits;
        }

        @Override
        public FractionDigitsStatement createDeclared(StmtContext<Integer, FractionDigitsStatement, ?> ctx) {
            return new FractionDigitsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Integer, FractionDigitsStatement> createEffective(
                StmtContext<Integer, FractionDigitsStatement, EffectiveStatement<Integer, FractionDigitsStatement>> ctx) {
            return new FractionDigitsEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public Integer getValue() {
        return argument();
    }
}
