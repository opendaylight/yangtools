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
import org.opendaylight.yangtools.yang.common.QName;

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
public final class YangQNameExpr implements YangExpr {
    private static final long serialVersionUID = 1L;

    private final QName qname;

    private YangQNameExpr(final QName qname) {
        this.qname = requireNonNull(qname);
    }

    public static YangQNameExpr of(final QName qname) {
        return new YangQNameExpr(qname);
    }

    public QName getQName() {
        return qname;
    }

    @Override
    public int hashCode() {
        return qname.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangQNameExpr && qname.equals(((YangQNameExpr) obj).qname);
    }

    @Override
    public String toString() {
        return qname.toString();
    }
}
