/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.math.BigInteger;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgument32(int value) implements MinElementsArgument {
    static final MinElementsArgument32 ZERO = new MinElementsArgument32(0);
    static final MinElementsArgument32 ONE = new MinElementsArgument32(1);
    static final MinElementsArgument32 TWO = new MinElementsArgument32(2);

    MinElementsArgument32 {
        if (value < 0) {
            throw new IllegalArgumentException("Negative value " + value);
        }
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            case MinElementsArgument32(var arg) -> Integer.compare(value, arg);
            // TODO: use _ when we have Java 22+
            case MinElementsArgument64 arg -> -1;
            case MinElementsArgumentBig arg -> -1;
        };
    }

    @Override
    public boolean matches(final int elementCount) {
        return elementCount >= value;
    }

    @Override
    public boolean matches(final long elementCount) {
        return elementCount >= value;
    }

    @Override
    public boolean matches(final BigInteger elementCount) {
        return elementCount.compareTo(BigInteger.valueOf(value)) >= 0;
    }

    @Override
    public OptionalInt intSize() {
        return OptionalInt.of(value);
    }

    @Override
    public OptionalLong longSize() {
        return OptionalLong.of(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}