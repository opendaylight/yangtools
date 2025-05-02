/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.OptionalInt;
import java.util.OptionalLong;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgument31(int value) implements MinElementsArgument {
    static final MinElementsArgument31 ZERO = new MinElementsArgument31(0);
    static final MinElementsArgument31 ONE = new MinElementsArgument31(1);
    static final MinElementsArgument31 TWO = new MinElementsArgument31(2);

    MinElementsArgument31 {
        if (value < 0) {
            throw new IllegalArgumentException("Negative value " + value);
        }
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            case MinElementsArgument31(var arg) -> Integer.compare(value, arg);
            // TODO: use _ when we have Java 22+
            case MinElementsArgument63 arg -> -1;
            case MinElementsArgumentStr arg -> -1;
        };
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