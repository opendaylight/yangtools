/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verify;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgumentBig(BigInteger lowerBig) implements MinElementsArgument {
    MinElementsArgumentBig {
        verify(lowerBig.compareTo(MaxElementsArgumentBig.LONG_MAX_VALUE) >= 0);
    }

    @Override
    public int lowerInt() {
        return Integer.MIN_VALUE;
    }

    @Override
    public long lowerLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            // TODO: use _ when we have Java 22+
            case MinElementsArgument32 arg -> 1;
            case MinElementsArgument64 arg -> 1;
            case MinElementsArgumentBig(var arg) -> lowerBig.compareTo(arg);
        };
    }

    @Override
    public String toString() {
        return lowerBig.toString();
    }
}