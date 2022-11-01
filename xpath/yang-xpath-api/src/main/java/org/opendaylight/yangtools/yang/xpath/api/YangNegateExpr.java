/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import org.eclipse.jdt.annotation.Nullable;

public final class YangNegateExpr implements YangExpr {
    @Serial
    private static final long serialVersionUID = 1L;

    private final YangExpr subExpr;

    private YangNegateExpr(final YangExpr subExpr) {
        this.subExpr = requireNonNull(subExpr);
    }

    public static YangNegateExpr of(final YangExpr subExpr) {
        return new YangNegateExpr(subExpr);
    }

    public YangExpr getSubExpr() {
        return subExpr;
    }

    @Override
    public int hashCode() {
        return subExpr.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangNegateExpr other && subExpr.equals(other.subExpr);
    }

    @Override
    public String toString() {
        return "-(" + subExpr + ")";
    }
}
