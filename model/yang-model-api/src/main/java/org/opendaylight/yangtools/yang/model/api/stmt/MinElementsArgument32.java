/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgument32(int lowerInt) implements MinElementsArgument {
    static final Interner<MinElementsArgument> INTERNER = Interners.newWeakInterner();

    MinElementsArgument32 {
        verify(lowerInt >= 0);
    }

    @Override
    public long lowerLong() {
        return lowerInt;
    }

    @Override
    public BigInteger lowerBig() {
        return BigInteger.valueOf(lowerInt);
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            case MinElementsArgument32(var arg) -> Integer.compare(lowerInt + 1, arg);
            // TODO: use _ when we have Java 22+
            case MinElementsArgument64 arg -> -1;
            case MinElementsArgumentBig arg -> -1;
        };
    }

    @Override
    public String toString() {
        return Integer.toString(lowerInt + 1);
    }
}