/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

abstract class AbstractYangXPathMathSupport<N extends YangNumberExpr<N, ?>> implements YangXPathMathSupport<N> {
    private final Class<N> numberClass;

    AbstractYangXPathMathSupport(final Class<N> numberClass) {
        this.numberClass = requireNonNull(numberClass);
    }

    @Override
    public final N negateNumber(final YangNumberExpr<?, ?> number) {
        checkArgument(numberClass.isInstance(requireNonNull(number)), "Expected %s have %s", numberClass, number);
        return doNegate(numberClass.cast(number));
    }


    @Override
    public final Optional<YangExpr> tryEvaluate(final YangBinaryOperator operator,
            final YangNumberExpr<?, ?> left, final YangNumberExpr<?, ?> right) {
        if (!numberClass.isInstance(left) || !numberClass.isInstance(right)) {
            requireNonNull(operator);
            requireNonNull(left);
            requireNonNull(right);
            return Optional.empty();
        }

        return Optional.of(evaluate(requireNonNull(operator), numberClass.cast(left), numberClass.cast(right)));
    }

    abstract N doNegate(N number);

    /**
     * Evaluate an  operator and its left- and right-handside.
     *
     * @param operator Operator to apply
     * @param left Left hand-side
     * @param right Right hand-side
     * @return Evaluation result
     */
    abstract YangExpr evaluate(YangBinaryOperator operator, N left, N right);
}
