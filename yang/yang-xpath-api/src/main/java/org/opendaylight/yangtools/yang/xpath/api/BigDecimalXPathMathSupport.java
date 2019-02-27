/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import java.math.BigDecimal;

final class BigDecimalXPathMathSupport extends AbstractYangXPathMathSupport<BigDecimalNumberExpr> {
    private static final BigDecimalXPathMathSupport INSTANCE = new BigDecimalXPathMathSupport();
    private static final BigDecimalNumberExpr ZERO = BigDecimalNumberExpr.of(BigDecimal.ZERO);
    private static final BigDecimalNumberExpr ONE = BigDecimalNumberExpr.of(BigDecimal.ONE);
    private static final BigDecimalNumberExpr TEN = BigDecimalNumberExpr.of(BigDecimal.TEN);

    private BigDecimalXPathMathSupport() {
        super(BigDecimalNumberExpr.class);
    }

    static BigDecimalXPathMathSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public BigDecimalNumberExpr createNumber(final String str) {
        switch (str) {
            case "0":
                return ZERO;
            case "1":
                return ONE;
            case "10":
                return TEN;
            default:
                return BigDecimalNumberExpr.of(new BigDecimal(str));
        }
    }

    @Override
    public BigDecimalNumberExpr createNumber(final int value) {
        switch (value) {
            case 0:
                return ZERO;
            case 1:
                return ONE;
            case 10:
                return TEN;
            default:
                return BigDecimalNumberExpr.of(BigDecimal.valueOf(value));
        }
    }

    @Override
    BigDecimalNumberExpr doNegate(final BigDecimalNumberExpr number) {
        return BigDecimalNumberExpr.of(number.getNumber().negate());
    }

    @Override
    YangExpr evaluate(final YangBinaryOperator operator, final BigDecimalNumberExpr left,
            final BigDecimalNumberExpr right) {
        final BigDecimal l = left.getNumber();
        final BigDecimal r = right.getNumber();

        final BigDecimal result;
        switch (operator) {
            case DIV:
                result = l.divide(r);
                break;
            case EQUALS:
                return YangBooleanConstantExpr.of(l.equals(r));
            case GT:
                return YangBooleanConstantExpr.of(l.compareTo(r) > 0);
            case GTE:
                return YangBooleanConstantExpr.of(l.compareTo(r) >= 0);
            case LT:
                return YangBooleanConstantExpr.of(l.compareTo(r) < 0);
            case LTE:
                return YangBooleanConstantExpr.of(l.compareTo(r) <= 0);
            case MINUS:
                result = l.subtract(r);
                break;
            case MOD:
                result = l.remainder(r);
                break;
            case MUL:
                result = l.multiply(r);
                break;
            case NOT_EQUALS:
                return YangBooleanConstantExpr.of(!l.equals(r));
            case PLUS:
                result = l.add(r);
                break;
            default:
                throw new IllegalStateException("Unhandled operator " + operator);
        }

        return BigDecimalNumberExpr.of(result);
    }
}
