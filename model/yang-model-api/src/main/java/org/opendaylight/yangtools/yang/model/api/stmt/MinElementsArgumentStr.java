/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.util.OptionalInt;
import java.util.OptionalLong;
import org.eclipse.jdt.annotation.NonNullByDefault;

// TODO: value once we have JEP-401
@NonNullByDefault
record MinElementsArgumentStr(String value) implements MinElementsArgument {
    MinElementsArgumentStr {
        requireNonNull(value);
    }

    // Exposed to fool CheckStyle into thinking the cause is propagated
    MinElementsArgumentStr(final String value, final NumberFormatException cause) {
        this(value);
        requireNonNull(cause);
    }

    @Override
    public int compareTo(final MinElementsArgument other) {
        // Note: we could do a single instanceof check, but we this provides null-hostility and exhaustiveness
        return switch (other) {
            case MinElementsArgument31 _, MinElementsArgument63 _ -> 1;
            case MinElementsArgumentStr(var arg) -> {
                // compare lengths first, establishing that "12" is greater than "2", for equal lengths lexicographic
                // order does the rest
                final var cmp = Integer.compare(value.length(), arg.length());
                yield cmp != 0 ? cmp : value.compareTo(arg);
            }
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
        return value;
    }
}