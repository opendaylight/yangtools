/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangNumberExpr.YangBigDecimal;

final class BigDecimalXPathParser extends XPathParser<YangBigDecimal> {
    private static final YangBigDecimal ZERO = YangBigDecimal.of(BigDecimal.ZERO);
    private static final YangBigDecimal ONE = YangBigDecimal.of(BigDecimal.ONE);
    private static final YangBigDecimal TEN = YangBigDecimal.of(BigDecimal.TEN);

    BigDecimalXPathParser(final QNameModule implicitNamespace, final Function<String, QNameModule> prefixes) {
        super(implicitNamespace, prefixes);
    }

    @Override
    YangBigDecimal createNumber(final String str) {
        switch (str) {
            case "0":
                return ZERO;
            case "1":
                return ONE;
            case "10":
                return TEN;
            default:
                return YangBigDecimal.of(new BigDecimal(str));
        }
    }

    @Override
    YangBigDecimal negateNumber(final YangBigDecimal number) {
        return YangBigDecimal.of(number.getNumber().negate());
    }

    @Override
    Optional<YangExpr> simplifyNumbers(final YangBinaryOperator operator, final YangBigDecimal left,
            final YangBigDecimal right) {
        final BigDecimal l = left.getNumber();
        final BigDecimal r = right.getNumber();

        final BigDecimal result;
        switch (operator) {
            case DIV:
                result = l.divide(r);
                break;
            case EQUALS:
                return of(l.equals(r));
            case GT:
                return of(l.compareTo(r) > 0);
            case GTE:
                return of(l.compareTo(r) >= 0);
            case LT:
                return of(l.compareTo(r) < 0);
            case LTE:
                return of(l.compareTo(r) <= 0);
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
                return of(!l.equals(r));
            case PLUS:
                result = l.add(r);
                break;
            default:
                throw new IllegalStateException("Unhandled operator " + operator);
        }

        return Optional.of(YangBigDecimal.of(result));
    }

    private static Optional<YangExpr> of(final boolean value) {
        return Optional.of(YangBooleanConstantExpr.of(value));
    }
}
