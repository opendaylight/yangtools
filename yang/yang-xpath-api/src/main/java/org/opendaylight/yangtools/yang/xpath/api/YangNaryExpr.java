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
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;

@Beta
public abstract class YangNaryExpr implements YangExpr {
    private static final long serialVersionUID = 1L;

    private final Set<YangExpr> expressions;

    YangNaryExpr(final Set<YangExpr> expressions) {
        this.expressions = requireNonNull(expressions);
    }

    public final Set<YangExpr> getExpressions() {
        return expressions;
    }

    public abstract YangNaryOperator getOperator();

    @Override
    public final int hashCode() {
        return Objects.hash(getOperator(), expressions);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YangNaryExpr)) {
            return false;
        }
        final YangNaryExpr other = (YangNaryExpr) obj;
        return getOperator().equals(other.getOperator()) && expressions.equals(other.expressions);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(YangNaryExpr.class)
                .add("operator", getOperator())
                .add("expressions", getExpressions())
                .toString();
    }
}
