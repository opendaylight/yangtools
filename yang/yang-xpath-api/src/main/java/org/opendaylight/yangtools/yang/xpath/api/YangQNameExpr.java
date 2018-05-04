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
 * An XPath QName expression. This is an exact QName, which cannot be converted to a string literal compatible with
 * XPath string representation, because it does not define a prefix/namespace mapping. It represents a strong binding
 * to a particular namespace at a particular revision.
 *
 * <p>
 * Parsers and users of this package are encouraged to use this class in place of {@link YangLiteralExpr} where
 * appropriate, as it retains type safety and more semantic context.
 *
 * @author Robert Varga
 */
@Beta
public class YangQNameExpr implements YangExpr {
    private static final long serialVersionUID = 1L;

    private final String literal;

    protected YangQNameExpr(final String literal) {
        this.literal = requireNonNull(literal);
    }

    public static final YangQNameExpr of(final String literal) {
        return new YangQNameExpr(literal);
    }

    public final String getLiteral() {
        return literal;
    }

    @Override
    public final int hashCode() {
        return literal.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangQNameExpr && literal.equals(((YangQNameExpr) obj).literal);
    }

    @Override
    public final String toString() {
        return literal;
    }
}
