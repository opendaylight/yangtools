/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.annotations.Beta;

/**
 * {@link XPathParser} number compliance knobs. This enumeration defines what assumptions the parser can make --
 * affecting its optimization properties around
 * <a href="https://en.wikipedia.org/wiki/Constant_folding">constant folding</a> when number expressions are involved.
 *
 * @author Robert Varga
 */
@Beta
public enum XPathParserNumberCompliance {
    /**
     * All number expressions are treated as {@code double}. This in spirit of XPath 1.0 -- any number expression stars
     * its life as a double, making all operations subject to IEEE754 rounding and range rules.
     */
    IEEE754,

    /**
     * All number expressions are treated as infinite-precision numbers. This follows the spirit of YANG 1.1 -- where
     * mostly have integral types and decimal64 mapped to BigDecimal. Non-decimal numbers are mapped either to
     * {@code int}, {@code long} or {@code BigInteger}.
     */
    EXACT,

    /*
     * FIXME: 3.0.0: specify and implement this:
     *
     * All number expressions are treated either as {@code org.opendaylight.yangtools.yang.common} types with precision
     * required to hold them. Decimal types are mapped to {@link Decimal64} with all range restrictions and rounding
     * error implied by it.
     */
    // ODL_COMMON,
}
