/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.math.BigDecimal;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A number-bearing expression.
 */
@Beta
public abstract class YangNumberExpr<T extends YangNumberExpr<T, N>, N extends Number> implements YangExpr {
    public static final class YangBigDecimal extends YangNumberExpr<YangBigDecimal, BigDecimal> {
        private static final long serialVersionUID = 1L;

        private final BigDecimal number;

        YangBigDecimal(final BigDecimal number) {
            this.number = requireNonNull(number);
        }

        @Override
        public BigDecimal getNumber() {
            return number;
        }

        @Override
        public int hashCode() {
            return number.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof YangBigDecimal
                    && number.equals(((YangBigDecimal) obj).number);
        }

        @Override
        public String toString() {
            return number.toString();
        }
    }

    public static final class YangDouble extends YangNumberExpr<YangDouble, Double> {
        private static final long serialVersionUID = 1L;

        private final double value;

        YangDouble(final double value) {
            this.value = value;
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
            return this == obj || obj instanceof YangDouble && value == ((YangDouble) obj).value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }


    private static final long serialVersionUID = 1L;

    YangNumberExpr() {
        // Hidden to prevent external subclassing
    }

    public static YangBigDecimal of(final BigDecimal number) {
        return new YangBigDecimal(number);
    }

    public static YangDouble of(final double value) {
        return new YangDouble(value);
    }

    public abstract N getNumber();
}
