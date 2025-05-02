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

    MinElementsArgumentStr(final String value, final NumberFormatException cause) {
        this(value);
        requireNonNull(cause);
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