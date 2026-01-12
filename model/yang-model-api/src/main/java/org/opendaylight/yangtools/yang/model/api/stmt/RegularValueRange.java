/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A regular {@link ValueRange}.
 */
// TODO: JEP-401 when we have it available
@NonNullByDefault
record RegularValueRange(Number lower, Number upper) implements ValueRange {
    RegularValueRange {
        requireNonNull(lower);
        requireNonNull(upper);
    }

    @Override
    public Number lowerBound() {
        return lower;
    }

    @Override
    public Number upperBound() {
        return upper;
    }

    @Override
    public String toString() {
        return lower + ".." + upper;
    }
}