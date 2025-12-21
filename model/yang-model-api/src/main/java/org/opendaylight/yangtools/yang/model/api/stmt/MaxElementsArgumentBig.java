/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;

/**
 * Used for {@code max-value} argument integer literal that would exceed the {@link Long.MAX_VALUE}.
 */
@NonNullByDefault
record MaxElementsArgumentBig(BigInteger value) implements Bounded {
    MaxElementsArgumentBig {
        requireNonNull(value);
    }

    @Override
    public boolean matches(final int elementCount) {
        return matches((long) elementCount);
    }

    @Override
    public boolean matches(final long elementCount) {
        return matches(BigInteger.valueOf(elementCount));
    }

    @Override
    public BigInteger asBigInteger() {
        return value;
    }

    @Override
    public int asSaturatedInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long asSaturatedLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int compareTo(final Bounded obj) {
        return switch (obj) {
            // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
            case MaxElementsArgument32 other -> 1;
            case MaxElementsArgument64 other -> 1;
            case MaxElementsArgumentBig other -> value.compareTo(other.value);
        };
    }

    @Override
    public String toString() {
        return value.toString();
    }
}