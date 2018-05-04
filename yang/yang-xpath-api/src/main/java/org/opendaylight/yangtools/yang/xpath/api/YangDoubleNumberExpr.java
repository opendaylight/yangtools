/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;

@Beta
public final class YangDoubleNumberExpr extends YangNumberExpr<YangDoubleNumberExpr, Double> {
    private static final long serialVersionUID = 1L;

    private final double value;

    private YangDoubleNumberExpr(final double value) {
        this.value = value;
    }

    public static YangDoubleNumberExpr of(final double value) {
        return new YangDoubleNumberExpr(value);
    }

    public double getValue() {
        return value;
    }

    @Override
    public Double getNumber() {
        return value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangDoubleNumberExpr && value == ((YangDoubleNumberExpr) obj).value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
