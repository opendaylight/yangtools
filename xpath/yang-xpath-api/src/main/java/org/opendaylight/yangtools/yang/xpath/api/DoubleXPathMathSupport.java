/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import java.io.Serial;

final class DoubleXPathMathSupport extends AbstractYangXPathMathSupport<DoubleNumberExpr> {
    static final DoubleXPathMathSupport INSTANCE = new DoubleXPathMathSupport();

    @Serial
    private static final long serialVersionUID = 1L;

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
        final var l = left.getValue();
        final var r = right.getValue();

        return switch (operator) {
            case DIV -> DoubleNumberExpr.of(l / r);
            case EQUALS -> YangBooleanConstantExpr.of(left.equals(right));
            case GT -> YangBooleanConstantExpr.of(l > r);
            case GTE -> YangBooleanConstantExpr.of(l >= r);
            case LT -> YangBooleanConstantExpr.of(l < r);
            case LTE -> YangBooleanConstantExpr.of(l <= r);
            case MINUS -> DoubleNumberExpr.of(l - r);
            case MOD -> DoubleNumberExpr.of(l % r);
            case MUL -> DoubleNumberExpr.of(l * r);
            case NOT_EQUALS -> YangBooleanConstantExpr.of(!left.equals(right));
            case PLUS -> DoubleNumberExpr.of(l + r);
        };
    }

    @Override
    protected Object readResolve() {
        return INSTANCE;
    }
}
