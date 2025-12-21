/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsValue.Bounded;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsValue.Unbounded;

/**
 * An argument to {@link MaxElementsStatement}. Can be either {@link Bounded} or {@link Unbounded}.
 */
@NonNullByDefault
public abstract sealed class MaxElementsValue implements Comparable<MaxElementsValue>, ElementsConstraint
        permits Bounded, Unbounded {
    /**
     * Singleton value representing an {@code unbounded} value.
     */
    public static final class Unbounded extends MaxElementsValue {
        /**
         * The singleton instance.
         */
        public static final Unbounded INSTANCE = new Unbounded();

        private Unbounded() {
            // Hidden on purpose
        }

        @Override
        public String toString() {
            return "unbounded";
        }

        @Override
        public int asSaturatedInt() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long asSaturatedLong() {
            return Long.MAX_VALUE;
        }

        @Override
        int hashCodeImpl() {
            return System.identityHashCode(this);
        }

        @Override
        boolean equalsImpl(final MaxElementsValue obj) {
            return obj instanceof Unbounded;
        }

        @Override
        public boolean matches(final int elementCout) {
            return true;
        }

        @Override
        public boolean matches(final long elementCout) {
            return true;
        }

        @Override
        public boolean matches(final BigInteger elementCout) {
            return true;
        }
    }

    /**
     * A {@link MaxElementsValue} with an upper bound on elements.
     *
     * @implSpec this interface establishes exclusion alongside
     */
    // N
    // All implementations are kept internal to establish the following invariant:

    public abstract static sealed class Bounded extends MaxElementsValue
            permits BigIntegerBounded, IntBounded, LongBounded {
        /**
         * {@return this value as a {@link BigInteger}, guaranteed to be greater than {@link BigInteger#ZERO}}
         */
        public abstract BigInteger asBigInteger();

        /**
         * Compare this instance to another object.
         *
         * @param obj the other instance
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        public abstract int compareTo(Bounded obj);

        @Override
        public final boolean matches(final BigInteger elementCount) {
            return elementCount.compareTo(asBigInteger()) == 0;
        }
    }

    /**
     * A Bounded argument whose upper bound does not exceed {@value Integer#MAX_VALUE}.
     */
    private static final class IntBounded extends Bounded {
        // TODO: extend range to unsigned
        static final BigInteger MAX_VALUE_BIG = BigInteger.valueOf(Integer.MAX_VALUE);
        static final IntBounded MAX_VALUE = new IntBounded(Integer.MAX_VALUE);
        static final IntBounded ONE = new IntBounded(1);
        static final IntBounded TWO = new IntBounded(2);

        private final int value;

        IntBounded(final int value) {
            if (value < 1) {
                throw new IllegalArgumentException("Invalid max-elements" + value);
            }
            this.value = value;
        }

        @Override
        public BigInteger asBigInteger() {
            return BigInteger.valueOf(value);
        }


        @Override
        public boolean matches(final int elementCount) {
            return elementCount <= value;
        }

        @Override
        public boolean matches(final long elementCount) {
            return elementCount <= value;
        }

        @Override
        public int asSaturatedInt() {
            return value;
        }

        @Override
        public long asSaturatedLong() {
            return value;
        }

        @Override
        public int compareTo(final Bounded obj) {
            return switch (obj) {
                case IntBounded other -> Integer.compare(value, other.value);
                // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
                case LongBounded other -> -1;
                case BigIntegerBounded other -> -1;
            };
        }

        @Override
        int hashCodeImpl() {
            return Integer.hashCode(value);
        }

        @Override
        boolean equalsImpl(final MaxElementsValue obj) {
            return obj instanceof IntBounded other && value == other.value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    /**
     * A Bounded argument whose upper bound does not exceed {@value Integer#MAX_VALUE}.
     */
    private static final class LongBounded extends Bounded {
        static final LongBounded MAX_VALUE = new LongBounded(Long.MAX_VALUE);
        static final BigInteger MAX_VALUE_BIG = MAX_VALUE.asBigInteger();

        private final long value;

        LongBounded(final long value) {
            verify(value > Integer.MAX_VALUE);
            this.value = value;
        }

        @Override
        public BigInteger asBigInteger() {
            return BigInteger.valueOf(value);
        }

        @Override
        public int asSaturatedInt() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long asSaturatedLong() {
            return value;
        }

        @Override
        public int compareTo(final Bounded obj) {
            return switch (obj) {
                // TODO: Java 22+: use https://openjdk.org/jeps/456 uunabed pattern
                case BigIntegerBounded other -> -1;
                case IntBounded other -> 1;
                case LongBounded other -> Long.compare(value, other.value);
            };
        }

        @Override
        int hashCodeImpl() {
            return Long.hashCode(value);
        }

        @Override
        boolean equalsImpl(final MaxElementsValue obj) {
            return obj instanceof LongBounded other && value == other.value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

        @Override
        public boolean matches(final int elementCount) {
            return elementCount <= value;
        }

        @Override
        public boolean matches(final long elementCount) {
            return elementCount <= value;
        }
    }

    /**
     * Used for {@code max-value} argument integer literal that would exceed the {@link Long.MAX_VALUE}.
     */
    private static final class BigIntegerBounded extends Bounded {
        static final BigInteger MIN_VALUE_BIG = BigInteger.valueOf(Long.MAX_VALUE);
        static final BigIntegerBounded MIN_VALUE = new BigIntegerBounded(MIN_VALUE_BIG);

        private final BigInteger value;

        BigIntegerBounded(final BigInteger value) {
            this.value = requireNonNull(value);
        }

        @Override
        public BigInteger asBigInteger() {
            return value;
        }

        @Override
        public int asSaturatedInt() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long asSaturatedLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean matches(final int elementCount) {
            return matches((long) elementCount);
        }

        @Override
        public boolean matches(final long elementCount) {
            return value.equals(BigInteger.valueOf(elementCount));
        }

        @Override
        public int compareTo(final Bounded obj) {
            return switch (obj) {
                case BigIntegerBounded other -> value.compareTo(other.value);
                case IntBounded other -> 1;
                case LongBounded other -> 1;
            };
        }

        @Override
        int hashCodeImpl() {
            return value.hashCode();
        }

        @Override
        boolean equalsImpl(final MaxElementsValue obj) {
            return obj instanceof BigIntegerBounded other && value.equals(other.value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final MaxElementsValue o) {
        return switch (o) {
            case Unbounded other -> this instanceof Unbounded ? 0 : -1;
            case Bounded other -> this instanceof Bounded bounded ? bounded.compareTo(other) : 1;
        };
    }

    /**
     * Return a {@link MaxElementsValue} value for specified argument string.
     *
     * @param argument the argument string
     * @return the {@link MaxElementsValue} value
     * @throws IllegalArgumentException if {@code argument} does not represent a valid value
     */
    public static MaxElementsValue ofArgument(final String argument) {
        if (argument.equals("unbounded")) {
            return Unbounded.INSTANCE;
        }

        final var value = new BigInteger(argument);
        if (value.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("non-positive max-elements");
        }

        if (value.equals(BigInteger.ONE)) {
            return IntBounded.ONE;
        }
        if (value.equals(BigInteger.TWO)) {
            return IntBounded.TWO;
        }
        final var intCmp = IntBounded.MAX_VALUE_BIG.compareTo(value);
        if (intCmp == 0) {
            return IntBounded.MAX_VALUE;
        }
        if (intCmp < 0) {
            return new IntBounded(value.intValueExact());
        }

        final var longCmp = LongBounded.MAX_VALUE_BIG.compareTo(value);
        if (longCmp == 0) {
            return LongBounded.MAX_VALUE;
        }
        if (longCmp < 0) {
            return new LongBounded(value.longValueExact());
        }

        return value.equals(BigIntegerBounded.MIN_VALUE_BIG) ? BigIntegerBounded.MIN_VALUE
            : new BigIntegerBounded(value);
    }

    @Override
    public final int hashCode() {
        return hashCodeImpl();
    }

    abstract int hashCodeImpl();

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof MaxElementsValue other && equalsImpl(other);
    }

    abstract boolean equalsImpl(MaxElementsValue obj);

    /**
     * {@return the canonical representation of this value}
     */
    @Override
    public abstract String toString();
}
