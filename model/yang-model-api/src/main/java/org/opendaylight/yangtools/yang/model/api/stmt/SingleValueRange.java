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
 * A {@link ValueRange} where lower and upper bounds are the same.
 */
@NonNullByDefault
record SingleValueRange(Number value) implements ValueRange {
    SingleValueRange {
        requireNonNull(value);
    }

    @Override
    public Number lowerBound() {
        return value;
    }

    @Override
    public Number upperBound() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}