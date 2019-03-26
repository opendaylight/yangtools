/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * {@link YangXPathParser} number compliance knobs. This enumeration defines what assumptions the parser can make --
 * affecting its optimization properties around
 * <a href="https://en.wikipedia.org/wiki/Constant_folding">constant folding</a> when number expressions are
 * involved.
 */
@Beta
public enum YangXPathMathMode {
    /**
     * All number expressions are treated as {@code double}. This in spirit of XPath 1.0 -- any number expression
     * starts its life as a double, making all operations subject to IEEE754 rounding and range rules.
     */
    IEEE754(DoubleXPathMathSupport.INSTANCE),

    /**
     * All number expressions are treated as infinite-precision numbers. This follows the spirit of YANG 1.1 --
     * where mostly have integral types and decimal64 mapped to BigDecimal. Non-decimal numbers are mapped either to
     * {@code int}, {@code long} or {@code BigInteger}.
     */
    EXACT(BigDecimalXPathMathSupport.INSTANCE);

    /*
     * FIXME: 4.0.0: specify and implement this:
     *
     * All number expressions are treated either as {@code org.opendaylight.yangtools.yang.common} types with
     * precision required to hold them. Decimal types are mapped to {@link Decimal64} with all range restrictions
     * and rounding error implied by it.
     */
    // ODL_COMMON;

    private YangXPathMathSupport support;

    YangXPathMathMode(final YangXPathMathSupport support) {
        this.support = requireNonNull(support);
    }

    /**
     * Return {@link YangXPathMathSupport} which provides support for the this mode.
     *
     * @return YangXPathMathSupport supporting this mode.
     */
    public YangXPathMathSupport getSupport() {
        return support;
    }
}
