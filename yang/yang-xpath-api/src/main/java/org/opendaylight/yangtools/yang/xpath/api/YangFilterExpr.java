/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;

@Beta
public class YangFilterExpr implements YangExpr, YangPredicateAware {
    private static final class WithPredicates extends YangFilterExpr {
        private static final long serialVersionUID = 1L;

        private final Set<YangExpr> predicates;

        WithPredicates(final YangExpr expr, final Set<YangExpr> predicates) {
            super(expr);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public Set<YangExpr> getPredicates() {
            return predicates;
        }
    }

    private static final long serialVersionUID = 1L;

    private final YangExpr expr;

    private YangFilterExpr(final YangExpr expr) {
        this.expr = requireNonNull(expr);
    }

    public static YangFilterExpr of(final YangExpr expr) {
        return new YangFilterExpr(expr);
    }

    public static YangFilterExpr of(final YangExpr expr, final YangExpr... predicates) {
        return of(expr, Arrays.asList(predicates));
    }

    public static YangFilterExpr of(final YangExpr expr, final Collection<YangExpr> predicates) {
        return predicates.isEmpty() ? of(expr) : new WithPredicates(expr, ImmutableSet.copyOf(predicates));
    }

    public final YangExpr getExpr() {
        return expr;
    }

    @Override
    public Set<YangExpr> getPredicates() {
        return ImmutableSet.of();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(expr, getPredicates());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YangFilterExpr)) {
            return false;
        }
        final YangFilterExpr other = (YangFilterExpr) obj;
        return expr.equals(((YangFilterExpr) obj).expr) && getPredicates().equals(other.getPredicates());
    }

    @Override
    public final String toString() {
        // FIXME: this is not right
        return "-(" + expr + ")";
    }
}
