/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;

final class DoubleNumberExpr extends YangNumberExpr {
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
        return DoubleXPathMathSupport.INSTANCE;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "")
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DoubleNumberExpr && bitEqual(((DoubleNumberExpr) obj).value);
    }

    private boolean bitEqual(final double other) {
        return Double.doubleToLongBits(value) == Double.doubleToLongBits(other);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}