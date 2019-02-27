/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A number-bearing expression.
 */
@Beta
public abstract class YangNumberExpr implements YangExpr {
    private static final long serialVersionUID = 1L;

    YangNumberExpr() {
        // Hidden to prevent external subclassing
    }

    public abstract Number getNumber();

    public abstract YangXPathMathSupport getSupport();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);
}
