/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgumentBig(BigInteger value) implements MinElementsArgument {
    MinElementsArgumentBig {
        requireNonNull(value);
    }

    // Exposed to fool CheckStyle into thinking the cause is propagated
    MinElementsArgumentBig(final BigInteger value, final NumberFormatException cause) {
        this(value);
        requireNonNull(cause);
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            // TODO: use _ when we have Java 22+
            case MinElementsArgument32 arg -> 1;
            case MinElementsArgument64 arg -> 1;
            case MinElementsArgumentBig(var arg) -> value.compareTo(arg);
        };
    }

    @Override
    public OptionalInt intSize() {
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong longSize() {
        return OptionalLong.empty();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean matches(final int elementCount) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean matches(final long elementCount) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean matches(final BigInteger elementCount) {
        // TODO Auto-generated method stub
        return false;
    }
}