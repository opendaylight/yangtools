/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.xpath;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * YANG XPath binary operator.
 *
 * @author Robert Varga
 */
@Beta
public enum YangBinaryOperator {
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    EQUALS("="),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    NOT_EQUALS("!="),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GT(">"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GTE(">="),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LT("<"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LTE("<="),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    PLUS("+"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    MINUS("-"),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MUL("*"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    DIV("div"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MOD("mod");

    private static final Map<String, YangBinaryOperator> STR_TO_OPER = Maps.uniqueIndex(Arrays.asList(values()),
        YangBinaryOperator::toString);

    private final String str;

    private YangBinaryOperator(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<YangBinaryOperator> forString(final String str) {
        return Optional.ofNullable(STR_TO_OPER.get(requireNonNull(str)));
    }
}
