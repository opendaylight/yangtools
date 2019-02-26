/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;

/**
 * A number-bearing expression.
 */
@Beta
public abstract class YangNumberExpr<T extends YangNumberExpr<T, N>, N extends Number> implements YangExpr {
    private static final long serialVersionUID = 1L;

    YangNumberExpr() {
        // Hidden to prevent external subclassing
    }

    public abstract N getNumber();

    public abstract YangXPathMathSupport<T> getSupport();
}
