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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

public final class YangVariableReferenceExpr implements YangExpr {
    @Serial
    private static final long serialVersionUID = 1L;

    private final QName variableName;

    private YangVariableReferenceExpr(final QName variableName) {
        this.variableName = requireNonNull(variableName);
    }

    public static YangVariableReferenceExpr of(final QName variableName) {
        return new YangVariableReferenceExpr(variableName);
    }

    public QName getVariableName() {
        return variableName;
    }

    @Override
    public int hashCode() {
        return variableName.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangVariableReferenceExpr other && variableName.equals(other.variableName);
    }

    @Override
    public String toString() {
        return "$" + variableName;
    }
}
