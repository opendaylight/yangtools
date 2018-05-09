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
import org.opendaylight.yangtools.yang.xpath.api.YangBigDecimalNumberExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;

final class ExactXPathParser extends XPathParser<YangBigDecimalNumberExpr> {

    ExactXPathParser(final QNameModule implicitNamespace, final Function<String, QNameModule> prefixes) {
        super(implicitNamespace, prefixes);
    }

    @Override
    YangBigDecimalNumberExpr createNumber(final String str) {
        // FIXME: specialize for integral types
        return YangBigDecimalNumberExpr.of(new BigDecimal(str));
    }

    @Override
    YangBigDecimalNumberExpr negateNumber(final YangBigDecimalNumberExpr number) {
        return YangBigDecimalNumberExpr.of(number.getNumber().negate());
    }

    @Override
    Optional<YangExpr> simplifyNumbers(final YangBinaryOperator operator, final YangBigDecimalNumberExpr left,
            final YangBigDecimalNumberExpr right) {
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

        return Optional.of(YangBigDecimalNumberExpr.of(result));
    }

    private static Optional<YangExpr> of(final boolean value) {
        return Optional.of(YangBooleanConstantExpr.of(value));
    }
}
