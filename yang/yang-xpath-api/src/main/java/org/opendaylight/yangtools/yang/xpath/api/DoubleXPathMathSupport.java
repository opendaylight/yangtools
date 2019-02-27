/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

final class DoubleXPathMathSupport extends AbstractYangXPathMathSupport<DoubleNumberExpr> {
    static final DoubleXPathMathSupport INSTANCE = new DoubleXPathMathSupport();

    private DoubleXPathMathSupport() {
        super(DoubleNumberExpr.class);
    }

    @Override
    public DoubleNumberExpr createNumber(final String str) {
        return DoubleNumberExpr.of(Double.parseDouble(str));
    }

    @Override
    public DoubleNumberExpr createNumber(final int value) {
        return DoubleNumberExpr.of(value);
    }

    @Override
    protected DoubleNumberExpr doNegateNumber(final DoubleNumberExpr number) {
        return DoubleNumberExpr.of(-number.getValue());
    }

    @Override
    protected YangExpr doEvaluate(final YangBinaryOperator operator, final DoubleNumberExpr left,
            final DoubleNumberExpr right) {
        final double l = left.getValue();
        final double r = right.getValue();

        final double result;
        switch (operator) {
            case DIV:
                result = l / r;
                break;
            case EQUALS:
                return YangBooleanConstantExpr.of(left.equals(right));
            case GT:
                return YangBooleanConstantExpr.of(l > r);
            case GTE:
                return YangBooleanConstantExpr.of(l >= r);
            case LT:
                return YangBooleanConstantExpr.of(l < r);
            case LTE:
                return YangBooleanConstantExpr.of(l <= r);
            case MINUS:
                result = l - r;
                break;
            case MOD:
                result = l % r;
                break;
            case MUL:
                result = l * r;
                break;
            case NOT_EQUALS:
                return YangBooleanConstantExpr.of(!left.equals(right));
            case PLUS:
                result = l + r;
                break;
            default:
                throw new IllegalStateException("Unhandled operator " + operator);
        }

        return DoubleNumberExpr.of(result);
    }
}
