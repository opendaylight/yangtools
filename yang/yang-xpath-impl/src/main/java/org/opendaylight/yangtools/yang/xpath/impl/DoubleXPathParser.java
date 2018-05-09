/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangNumberExpr.YangDouble;

final class DoubleXPathParser extends XPathParser<YangDouble> {

    DoubleXPathParser(final QNameModule implicitNamespace, final Function<String, QNameModule> prefixes) {
        super(implicitNamespace, prefixes);
    }

    @Override
    YangDouble createNumber(final String str) {
        return YangDouble.of(Double.parseDouble(str));
    }

    @Override
    YangDouble negateNumber(final YangDouble number) {
        return YangDouble.of(-number.getValue());
    }

    @Override
    Optional<YangExpr> simplifyNumbers(final YangBinaryOperator operator, final YangDouble left,
            final YangDouble right) {
        final double l = left.getValue();
        final double r = right.getValue();

        final double result;
        switch (operator) {
            case DIV:
                result = l / r;
                break;
            case EQUALS:
                return of(l == r);
            case GT:
                return of(l > r);
            case GTE:
                return of(l >= r);
            case LT:
                return of(l < r);
            case LTE:
                return of(l <= r);
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
                return of(l != r);
            case PLUS:
                result = l + r;
                break;
            default:
                throw new IllegalStateException("Unhandled operator " + operator);
        }

        return Optional.of(YangDouble.of(result));
    }

    private static Optional<YangExpr> of(final boolean value) {
        return Optional.of(YangBooleanConstantExpr.of(value));
    }
}
