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
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Expressions which evaluate to a logical {@code true} or {@code false}. These expressions are equivalent to the result
 * returned by {@code true()} and {@code false()} functions defined in XPath 1.0.
 *
 * <p>
 * They also map these functions' names to the constant pool under their {@link YangFunctionCallExpr#getName()}
 * identity. All users should use these constants in favor of their equivalent function calls.
 *
 * @author Robert Varga
 */
@Beta
public enum YangBooleanConstantExpr implements YangConstantExpr<Boolean> {
    /**
     * A constant {@code false} expression.
     */
    FALSE(Boolean.FALSE, YangFunction.FALSE, "false"),
    /**
     * A constant {@code true} expression.
     */
    TRUE(Boolean.TRUE, YangFunction.TRUE, "true");

    private final YangFunctionCallExpr function;
    private final YangLiteralExpr literal;
    private final Boolean value;

    @SuppressWarnings("null")
    YangBooleanConstantExpr(final @Nullable Boolean value, final YangFunction function, final String literal) {
        this.value = requireNonNull(value);
        this.function = YangFunctionCallExpr.of(function.getIdentifier());
        this.literal = YangLiteralExpr.of(literal);
    }

    @Override
    public QName getIdentifier() {
        return function.getName();
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    /**
     * Convert this constant into the equivalent function. This function is provided for bridging purposes only.
     *
     * @return Equivalent function invocation.
     */
    public YangFunctionCallExpr asFunction() {
        return function;
    }

    /**
     * Convert this constant into a string literal, i.e. the result of calling {@code string(boolean)} function on this
     * constant.
     *
     * @return Literal expression.
     */
    public YangLiteralExpr asStringLiteral() {
        return literal;
    }

    public static YangBooleanConstantExpr of(final boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static Optional<YangFunctionCallExpr> forFunctionName(final String functionName) {
        switch (functionName) {
            case "false":
                return Optional.of(FALSE.function);
            case "true":
                return Optional.of(TRUE.function);
            default:
                return Optional.empty();
        }
    }
}
