/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import java.io.Serial;
import java.math.BigDecimal;

final class BigDecimalXPathMathSupport extends AbstractYangXPathMathSupport<BigDecimalNumberExpr> {
    static final BigDecimalXPathMathSupport INSTANCE = new BigDecimalXPathMathSupport();

    @Serial
    private static final long serialVersionUID = 1L;
    private static final BigDecimalNumberExpr ZERO = BigDecimalNumberExpr.of(BigDecimal.ZERO);
    private static final BigDecimalNumberExpr ONE = BigDecimalNumberExpr.of(BigDecimal.ONE);
    private static final BigDecimalNumberExpr TEN = BigDecimalNumberExpr.of(BigDecimal.TEN);

    private BigDecimalXPathMathSupport() {
        super(BigDecimalNumberExpr.class);
    }

    @Override
    public BigDecimalNumberExpr createNumber(final String str) {
        return switch (str) {
            case "0" -> ZERO;
            case "1" -> ONE;
            case "10" -> TEN;
            default -> BigDecimalNumberExpr.of(new BigDecimal(str));
        };
    }

    @Override
    public BigDecimalNumberExpr createNumber(final int value) {
        return switch (value) {
            case 0 -> ZERO;
            case 1 -> ONE;
            case 10 -> TEN;
            default -> BigDecimalNumberExpr.of(BigDecimal.valueOf(value));
        };
    }

    @Override
    protected BigDecimalNumberExpr doNegateNumber(final BigDecimalNumberExpr number) {
        return BigDecimalNumberExpr.of(number.getNumber().negate());
    }

    @Override
    protected YangExpr doEvaluate(final YangBinaryOperator operator, final BigDecimalNumberExpr left,
            final BigDecimalNumberExpr right) {
        final var l = left.getNumber();
        final var r = right.getNumber();

        return switch (operator) {
            case DIV -> BigDecimalNumberExpr.of(l.divide(r));
            case EQUALS -> YangBooleanConstantExpr.of(l.equals(r));
            case GT -> YangBooleanConstantExpr.of(l.compareTo(r) > 0);
            case GTE -> YangBooleanConstantExpr.of(l.compareTo(r) >= 0);
            case LT -> YangBooleanConstantExpr.of(l.compareTo(r) < 0);
            case LTE -> YangBooleanConstantExpr.of(l.compareTo(r) <= 0);
            case MINUS -> BigDecimalNumberExpr.of(l.subtract(r));
            case MOD -> BigDecimalNumberExpr.of(l.remainder(r));
            case MUL -> BigDecimalNumberExpr.of(l.multiply(r));
            case NOT_EQUALS -> YangBooleanConstantExpr.of(!l.equals(r));
            case PLUS -> BigDecimalNumberExpr.of(l.add(r));
        };
    }

    @Override
    protected Object readResolve() {
        return INSTANCE;
    }
}
