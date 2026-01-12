/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * YANG specification of a numeric value range. This object is used for {@link LengthStatement} and
 * {@link RangeStatement}.
 */
@NonNullByDefault
public sealed interface ValueRange extends Immutable permits RegularValueRange, SingleValueRange {
    /**
     * {@return this range's lower bound}
     */
    Number lowerBound();

    /**
     * {@return this range's upper bound}
     */
    Number upperBound();

    static ValueRange of(final Number value) {
        return new SingleValueRange(value);
    }

    static ValueRange of(final Number lower, final Number upper) {
        return lower.equals(upper) ? of(lower) : new RegularValueRange(lower, upper);
    }
}
