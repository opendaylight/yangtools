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
record MinElementsArgument64(long lowerLong) implements MinElementsArgument {
    MinElementsArgument64 {
        verify(lowerLong >= Integer.MAX_VALUE);
    }

    @Override
    public int lowerInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public BigInteger lowerBig() {
        return BigInteger.valueOf(lowerLong);
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        return switch (other) {
            case MinElementsArgument32 arg -> 1;
            case MinElementsArgument64 arg -> Long.compare(lowerLong, arg.lowerLong);
            case MinElementsArgumentBig arg -> -1;
        };
    }

    @Override
    public String toString() {
        return Long.toString(lowerLong + 1);
    }

}