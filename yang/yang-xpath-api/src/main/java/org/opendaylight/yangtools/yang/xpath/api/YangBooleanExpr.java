/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.xpath;

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
public enum YangBooleanExpr implements YangConstantExpr<Boolean> {
    /**
     * A constant {@code false} expression.
     */
    FALSE(Boolean.FALSE, YangFunction.FALSE),
    /**
     * A constant {@code true} expression.
     */
    TRUE(Boolean.TRUE, YangFunction.TRUE);

    private YangFunctionCallExpr function;
    private Boolean value;

    @SuppressWarnings("null")
    YangBooleanExpr(final @Nullable Boolean value, final YangFunction function) {
        this.value = requireNonNull(value);
        this.function = new YangFunctionCallExprBuilder().name(function.getIdentifier()).build();
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

    public static YangBooleanExpr of(final boolean bool) {
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
