/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import java.util.Optional;

@Beta
public interface YangXPathMathSupport<N extends YangNumberExpr<N, ?>> {
    /**
     * Create a {@link YangNumberExpr} backed by specified string.
     *
     * @param str String
     * @return number expression
     * @throws NullPointerException if {@code str} is null
     * @throws NumberFormatException if the string does not represent a valid number
     */
    N createNumber(String str);

    /**
     * Create a {@link YangNumberExpr} for specified integer.
     *
     * @param value integer value
     * @return number expression
     */
    N createNumber(int value);

    /**
     * Create a {@link YangNumberExpr} representing the negated value of a number.
     *
     * @param number input number
     * @return negated number expression
     * @throws NullPointerException if {@code number} is null
     * @throws IllegalArgumentException if {@code number} has unrecognized type
     */
    N negateNumber(YangNumberExpr<?, ?> number);

    /**
     * Attempt to evaluate an  operator and its left- and right-handside.
     *
     * @param operator Operator to apply
     * @param left Left hand-side
     * @param right Right hand-side
     * @return Evaluation result, if evaluation succeeded
     * @throws NullPointerException if any of the arguments is null
     */
    Optional<YangExpr> tryEvaluate(YangBinaryOperator operator, YangNumberExpr<?, ?> left, YangNumberExpr<?, ?> right);
}
