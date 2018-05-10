/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xpath;

import static com.google.common.base.Verify.verify;

import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.xpath.YangBinaryOperator;
import org.opendaylight.yangtools.yang.model.api.xpath.YangBooleanExpr;
import org.opendaylight.yangtools.yang.model.api.xpath.YangExpr;
import org.opendaylight.yangtools.yang.model.api.xpath.YangNumberExpr;
import org.opendaylight.yangtools.yang.model.api.xpath.YangNumberExprBuilder;

final class DoubleXPathParser extends XPathParser {

    DoubleXPathParser(final QNameModule implicitNamespace, final Function<String, QNameModule> prefixes) {
        super(implicitNamespace, prefixes);
    }

    @Override
    public XPathParserNumberCompliance getNumberCompliance() {
        return XPathParserNumberCompliance.IEEE754;
    }

    @Override
    YangNumberExpr createNumber(final String str) {
        return new YangNumberExprBuilder().number(Double.valueOf(str)).build();
    }

    @Override
    YangNumberExpr negateNumber(final YangNumberExpr number) {
        return new YangNumberExprBuilder().number(-coerceDouble(number)).build();
    }

    @Override
    Optional<YangExpr> simplifyNumbers(final YangBinaryOperator operator, final YangNumberExpr left,
            final YangNumberExpr right) {
        final double l = coerceDouble(left);
        final double r = coerceDouble(right);

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

        return Optional.of(new YangNumberExprBuilder().number(result).build());
    }

    private static Optional<YangExpr> of(final boolean value) {
        return Optional.of(YangBooleanExpr.of(value));
    }

    private static double coerceDouble(final YangNumberExpr expr) {
        final Number number = expr.getNumber();
        verify(number instanceof Double);
        return number.doubleValue();
    }
}
