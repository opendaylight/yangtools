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
import java.util.Collection;

/**
 * YANG XPath binary operator.
 *
 * @author Robert Varga
 */
@Beta
public enum YangNaryOperator {
    /**
     * Logical 'and' operator on operands.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AndExpr">AndExpr</a>
     */
    AND("and"),
    /**
     * Logical 'or' operator on operands.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-OrExpr">OrExpr</a>
     */
    OR("or"),
    /**
     * Set union operator on operands.
     *
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-UnionExpr">UnionExpr</a>
     */
    UNION("|");

    private final String str;

    YangNaryOperator(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public String toString() {
        return str;
    }

    public YangExpr exprWith(final Collection<YangExpr> exprs) {
        final ImmutableSet<YangExpr> set = ImmutableSet.copyOf(exprs);
        return set.size() == 1 ? set.iterator().next() : new YangNaryExpr(this, set);
    }
}
