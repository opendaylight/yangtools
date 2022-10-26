/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;

/**
 * YANG specification of a numeric value range. This object is used for {@link LengthStatement} and
 * {@link RangeStatement}.
 */
public abstract sealed class ValueRange {
    private static final class Singleton extends ValueRange {
        private final @NonNull Number value;

        Singleton(final @NonNull Number value) {
            this.value = requireNonNull(value);
        }

        @Override
        public @NonNull Number lowerBound() {
            return value;
        }

        @Override
        public @NonNull Number upperBound() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private static final class Range extends ValueRange {
        private final @NonNull Number lower;
        private final @NonNull Number upper;

        Range(final Number lower, final Number upper) {
            this.lower = requireNonNull(lower);
            this.upper = requireNonNull(upper);
        }

        @Override
        public @NonNull Number lowerBound() {
            return lower;
        }

        @Override
        public @NonNull Number upperBound() {
            return upper;
        }

        @Override
        public String toString() {
            return lower + ".." + upper;
        }
    }

    public static @NonNull ValueRange of(final @NonNull Number value) {
        return new Singleton(value);
    }

    public static @NonNull ValueRange of(final @NonNull Number lower, final @NonNull Number upper) {
        return lower.equals(upper) ? of(lower) : new Range(lower, upper);
    }

    public abstract @NonNull Number lowerBound();

    public abstract @NonNull Number upperBound();

    @Override
    public final int hashCode() {
        return Objects.hash(lowerBound(), upperBound());
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof ValueRange other
            && lowerBound().equals(other.lowerBound()) && upperBound().equals(other.upperBound());
    }
}
