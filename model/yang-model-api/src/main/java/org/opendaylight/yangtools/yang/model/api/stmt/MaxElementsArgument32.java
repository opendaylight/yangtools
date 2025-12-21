/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A Bounded argument whose upper bound does not exceed {@value Integer#MAX_VALUE}.
 */
// FIXME: use JEP-401 when available
@NonNullByDefault
record MaxElementsArgument32(int value) implements MaxElementsArgument.Bounded {
    // TODO: extend range to unsigned
    private static final BigInteger MAX_VALUE_BIG = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final MaxElementsArgument32 MAX_VALUE = new MaxElementsArgument32(Integer.MAX_VALUE);

    // Convenience placement outside of the main interface
    private static final Interner<MaxElementsArgument.Bounded> INTERNER = Interners.newWeakInterner();

    MaxElementsArgument32 {
        if (value < 1) {
            throw new IllegalArgumentException("Invalid max-elements" + value);
        }
    }

    static MaxElementsArgument.Bounded ofArgument(final BigInteger argument) {
        final var cmp = MAX_VALUE_BIG.compareTo(argument);
        if (cmp == 0) {
            return MaxElementsArgument32.MAX_VALUE;
        }
        return INTERNER.intern(cmp < 0
            ? new MaxElementsArgument32(argument.intValueExact())
            : MaxElementsArgument64.ofArgument(argument));
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
        return value;
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
    public int compareTo(final Bounded obj) {
        return switch (obj) {
            case MaxElementsArgument32 other -> Integer.compare(value, other.value);
            // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
            case MaxElementsArgument64 other -> -1;
            case MaxElementsArgumentBig other -> -1;
        };
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
