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
record MinElementsArgument63(long value) implements MinElementsArgument {
    MinElementsArgument63 {
        if (value < 0) {
            throw new IllegalArgumentException("Negative value " + value);
        }
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        return switch (other) {
            case MinElementsArgument31 arg -> 1;
            case MinElementsArgument63(var arg) -> Long.compare(value, arg);
            case MinElementsArgumentStr arg -> -1;
        };
    }

    @Override
    public OptionalInt intSize() {
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong longSize() {
        return OptionalLong.of(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}