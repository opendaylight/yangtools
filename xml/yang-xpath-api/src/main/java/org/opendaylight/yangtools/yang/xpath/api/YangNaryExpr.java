/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link YangExpr} combining a {@link YangNaryOperator} with a set of expressions.
 */
public final class YangNaryExpr implements YangExpr {
    @Serial
    private static final long serialVersionUID = 1L;

    private final YangNaryOperator operator;
    private final ImmutableSet<YangExpr> expressions;

    YangNaryExpr(final YangNaryOperator operator, final ImmutableSet<YangExpr> expressions) {
        this.operator = requireNonNull(operator);
        this.expressions = requireNonNull(expressions);
    }

    public Set<YangExpr> getExpressions() {
        return expressions;
    }

    public YangNaryOperator getOperator() {
        return operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperator(), expressions);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangNaryExpr other && getOperator().equals(other.getOperator())
            && expressions.equals(other.expressions);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(YangNaryExpr.class)
                .add("operator", operator)
                .add("expressions", expressions)
                .toString();
    }
}
