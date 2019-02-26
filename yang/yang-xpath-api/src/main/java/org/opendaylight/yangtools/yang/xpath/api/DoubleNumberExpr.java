/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import org.eclipse.jdt.annotation.Nullable;

final class DoubleNumberExpr extends YangNumberExpr<DoubleNumberExpr, Double> {
    private static final long serialVersionUID = 1L;

    private final double value;

    private DoubleNumberExpr(final double value) {
        this.value = value;
    }

    static DoubleNumberExpr of(final double value) {
        return new DoubleNumberExpr(value);
    }

    double getValue() {
        return value;
    }

    @Override
    public Double getNumber() {
        return value;
    }

    @Override
    public DoubleXPathMathSupport getSupport() {
        return DoubleXPathMathSupport.getInstance();
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DoubleNumberExpr && value == ((DoubleNumberExpr) obj).value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}