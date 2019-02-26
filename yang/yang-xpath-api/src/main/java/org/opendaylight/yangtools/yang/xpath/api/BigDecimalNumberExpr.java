/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import org.eclipse.jdt.annotation.Nullable;

final class BigDecimalNumberExpr extends YangNumberExpr<BigDecimalNumberExpr, BigDecimal> {
    private static final long serialVersionUID = 1L;

    private final BigDecimal number;

    private BigDecimalNumberExpr(final BigDecimal number) {
        this.number = requireNonNull(number);
    }

    static BigDecimalNumberExpr of(final BigDecimal number) {
        return new BigDecimalNumberExpr(number);
    }

    @Override
    public BigDecimal getNumber() {
        return number;
    }

    @Override
    public BigDecimalXPathMathSupport getSupport() {
        return BigDecimalXPathMathSupport.getInstance();
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof BigDecimalNumberExpr
                && number.equals(((BigDecimalNumberExpr) obj).number);
    }

    @Override
    public String toString() {
        return number.toString();
    }
}