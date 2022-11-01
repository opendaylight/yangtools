/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A binary expression formed of a {@link #getLeftExpr()}, an {@link #getOperator()} and a {@link #getRightExpr()}.
 */
public abstract sealed class YangBinaryExpr implements YangExpr permits YangBinaryOperator.Expr {
    @Serial
    private static final long serialVersionUID = 1L;

    private final YangExpr leftExpr;
    private final YangExpr rightExpr;

    YangBinaryExpr(final YangExpr leftExpr, final YangExpr rightExpr) {
        this.leftExpr = requireNonNull(leftExpr);
        this.rightExpr = requireNonNull(rightExpr);
    }

    public final YangExpr getLeftExpr() {
        return leftExpr;
    }

    public final YangExpr getRightExpr() {
        return rightExpr;
    }

    public abstract YangBinaryOperator getOperator();

    @Override
    public final int hashCode() {
        return Objects.hash(leftExpr, rightExpr, getOperator());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangBinaryExpr other && getOperator().equals(other.getOperator())
            && leftExpr.equals(other.leftExpr) && rightExpr.equals(other.rightExpr);
    }

    @Override
    public final String toString() {
        return leftExpr.toString() + " " + getOperator() + " " + rightExpr;
    }
}
