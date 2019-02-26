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

/**
 * An XPath literal expression.
 *
 * <p>
 * Note that a literal may be required to hold a value of {@code instance-identifier} or {@code identityref} type,
 * when the corresponding {@link YangXPathExpression} was parsed from reference String specification defined in RFC7950.
 * When such conversion is required, it should be performed through
 * {@link YangXPathExpression#interpretAsQName(YangLiteralExpr)} or
 * {@link YangXPathExpression#interpretAsInstanceIdentifier(YangLiteralExpr)}.
 *
 * <p>
 * A more type-safe alternative is {@link YangQNameExpr}, which should be preferred and used whenever possible.
 *
 * @author Robert Varga
 */
@Beta
public final class YangLiteralExpr implements YangExpr {
    private static final long serialVersionUID = 1L;
    private static final YangLiteralExpr EMPTY = new YangLiteralExpr("");

    private final String literal;

    protected YangLiteralExpr(final String literal) {
        this.literal = requireNonNull(literal);
    }

    public static YangLiteralExpr empty() {
        return EMPTY;
    }

    public static YangLiteralExpr of(final String literal) {
        return literal.isEmpty() ? EMPTY : new YangLiteralExpr(literal);
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

    protected Object readResolve() {
        return literal.isEmpty() ? EMPTY : this;
    }
}
