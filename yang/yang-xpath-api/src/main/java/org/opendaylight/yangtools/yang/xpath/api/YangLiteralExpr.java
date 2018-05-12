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
import org.eclipse.jdt.annotation.Nullable;

@Beta
public final class YangLiteralExpr implements YangExpr {
    private static final long serialVersionUID = 1L;

    private final String literal;

    private YangLiteralExpr(final String literal) {
        this.literal = requireNonNull(literal);
    }

    public static YangLiteralExpr of(final String literal) {
        return new YangLiteralExpr(literal);
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public int hashCode() {
        return literal.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangLiteralExpr && literal.equals(((YangLiteralExpr) obj).literal);
    }

    @Override
    public String toString() {
        return literal;
    }
}
