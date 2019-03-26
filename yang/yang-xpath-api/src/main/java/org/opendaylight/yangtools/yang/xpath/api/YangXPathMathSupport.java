/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.util.Optional;

/**
 * Interface supporting mathematical operations. This interface should be implemented by subclassing
 * {@link AbstractYangXPathMathSupport}, which provides type safety guards.
 *
 * @author Robert Varga
 */
@Beta
public interface YangXPathMathSupport extends Serializable {
    /**
     * Create a {@link YangNumberExpr} backed by specified string.
     *
     * @param str String
     * @return number expression
     * @throws NullPointerException if {@code str} is null
     * @throws NumberFormatException if the string does not represent a valid number
     */
    YangNumberExpr createNumber(String str);

    /**
     * Create a {@link YangNumberExpr} for specified integer.
     *
     * @param value integer value
     * @return number expression
     */
    YangNumberExpr createNumber(int value);

    /**
     * Create a {@link YangNumberExpr} representing the negated value of a number.
     *
     * @param number input number
     * @return negated number expression
     * @throws NullPointerException if {@code number} is null
     * @throws IllegalArgumentException if {@code number} has unrecognized type
     */
    YangNumberExpr negateNumber(YangNumberExpr number);

    /**
     * Attempt to evaluate an  operator and its left- and right-handside.
     *
     * @param operator Operator to apply
     * @param left Left hand-side
     * @param right Right hand-side
     * @return Evaluation result, if evaluation succeeded
     * @throws NullPointerException if any of the arguments is null
     */
    Optional<YangExpr> tryEvaluate(YangBinaryOperator operator, YangNumberExpr left, YangNumberExpr right);
}
