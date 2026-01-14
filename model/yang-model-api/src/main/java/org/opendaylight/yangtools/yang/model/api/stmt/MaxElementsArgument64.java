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
// TODO: use JEP-401 when available
@NonNullByDefault
record MaxElementsArgument64(long asSaturatedLong) implements Bounded {
    MaxElementsArgument64 {
        verify(asSaturatedLong > Integer.MAX_VALUE);
    }

    @Override
    public boolean matches(final int elementCount) {
        return elementCount <= asSaturatedLong;
    }

    @Override
    public boolean matches(final long elementCount) {
        return elementCount <= asSaturatedLong;
    }

    @Override
    public int asSaturatedInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public BigInteger asBigInteger() {
        return BigInteger.valueOf(asSaturatedLong);
    }

    @Override
    public int compareToOther(final Bounded obj) {
        return switch (obj) {
            // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
            case MaxElementsArgument32 other -> 1;
            case MaxElementsArgument64 other -> Long.compare(asSaturatedLong, other.asSaturatedLong);
            case MaxElementsArgumentBig other -> -1;
        };
    }

    @Override
    public String toString() {
        return Long.toString(asSaturatedLong);
    }
}