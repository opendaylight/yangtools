/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;

/**
 * YANG XPath binary operator.
 */
public enum YangBinaryOperator {
    /**
     * Operands are equal.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    EQUALS("="),
    /**
     * Operands do not equal.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    NOT_EQUALS("!="),

    /**
     * Left-hand operand is greater than right-hand operand.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GT(">"),
    /**
     * Left-hand operand is greater than or equal to right-hand operand.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GTE(">="),
    /**
     * Left-hand operand is less than right-hand operand.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LT("<"),
    /**
      * Left-hand operand is less than or equal to right-hand operand.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LTE("<="),

    /**
     * Arithmetic addition.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    PLUS("+"),
    /**
     * Arithmetic subtraction.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    MINUS("-"),

    /**
     * Arithmetic multiplication.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MUL("*"),
    /**
     * Arithmetic division.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    DIV("div"),
    /**
     * Arithmetic modulus after truncating division.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MOD("mod");

    @SuppressFBWarnings(value = "SE_INNER_CLASS", justification = "Outer class is a retained enumeration")
    final class Expr extends YangBinaryExpr {
        @Serial
        private static final long serialVersionUID = 1L;

        Expr(final YangExpr leftExpr, final YangExpr rightExpr) {
            super(leftExpr, rightExpr);
        }

        @Override
        public YangBinaryOperator getOperator() {
            return YangBinaryOperator.this;
        }
    }

    private final String str;

    YangBinaryOperator(final String str) {
        this.str = requireNonNull(str);
    }

    public YangBinaryExpr exprWith(final YangExpr leftExpr, final YangExpr rightExpr) {
        return new Expr(leftExpr, rightExpr);
    }

    @Override
    public String toString() {
        return str;
    }
}
