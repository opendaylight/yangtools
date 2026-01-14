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
 * Used for {@code max-value} argument integer literal that would exceed the {@link Long.MAX_VALUE}.
 */
// TODO: use JEP-401 when available
@NonNullByDefault
record MaxElementsArgumentBig(BigInteger asBigInteger) implements Bounded {
    static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

    MaxElementsArgumentBig {
        verify(asBigInteger.compareTo(LONG_MAX_VALUE) > 0);
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
    public int compareToOther(final Bounded obj) {
        return switch (obj) {
            // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
            case MaxElementsArgument32 other -> 1;
            case MaxElementsArgument64 other -> 1;
            case MaxElementsArgumentBig other -> asBigInteger.compareTo(other.asBigInteger);
        };
    }

    @Override
    public String toString() {
        return asBigInteger.toString();
    }
}