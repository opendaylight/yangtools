/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verify;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;

/**
 * A Bounded argument whose upper bound does not exceed {@value Integer#MAX_VALUE}.
 */
// FIXME: use JEP-401 when available
@NonNullByDefault
record MaxElementsArgument64(long value) implements Bounded {
    private static final MaxElementsArgument64 MAX_VALUE = new MaxElementsArgument64(Long.MAX_VALUE);
    private static final BigInteger MAX_VALUE_BIG = MAX_VALUE.asBigInteger();

    MaxElementsArgument64 {
        verify(value > Integer.MAX_VALUE);
    }

    static MaxElementsArgument.Bounded ofArgument(final BigInteger argument) {
        final var cmp = MAX_VALUE_BIG.compareTo(argument);
        if (cmp == 0) {
            return MAX_VALUE;
        }
        return cmp > 0 ? new MaxElementsArgument64(argument.longValueExact()) : new MaxElementsArgumentBig(argument);
    }

    @Override
    public boolean matches(final int elementCount) {
        return elementCount <= value;
    }

    @Override
    public boolean matches(final long elementCount) {
        return elementCount <= value;
    }

    @Override
    public int asSaturatedInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long asSaturatedLong() {
        return value;
    }

    @Override
    public BigInteger asBigInteger() {
        return BigInteger.valueOf(value);
    }

    @Override
    public int compareToBounded(final Bounded obj) {
        return switch (obj) {
            // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
            case MaxElementsArgument32 other -> 1;
            case MaxElementsArgument64 other -> Long.compare(value, other.value);
            case MaxElementsArgumentBig other -> -1;
        };
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}