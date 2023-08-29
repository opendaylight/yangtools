/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility methods for converting Java and Guava integer types to their {@link Uint8}, {@link Uint16}, {@link Uint32}
 * and {@link Uint64} equivalents. While individual types provide these through their {@code valueOf()} methods, this
 * class allows dealing with multiple types through a static import:
 *
 * <pre>
 *   <code>
 *     import static org.opendaylight.yangtools.yang.common.UintConversions.fromJava;
 *
 *     Uint16 two = fromJava(32);
 *     Uint32 one = fromJava(32L);
 *   </code>
 * </pre>
 */
@NonNullByDefault
public final class UintConversions {
    private UintConversions() {
        // Hidden on purpose
    }

    /**
     * Convert a {@code short} in range 0-255 to an Uint8.
     *
     * @param value value
     * @return Uint8 object
     * @throws IllegalArgumentException if value is less than zero or greater than 255
     */
    public static Uint8 fromJava(final short value) {
        return Uint8.valueOf(value);
    }

    /**
     * Convert an {@code int} in range 0-65535 to a Uint16.
     *
     * @param value value
     * @return Uint16 object
     * @throws IllegalArgumentException if value is less than zero or greater than 65535.
     */
    public static Uint16 fromJava(final int value) {
        return Uint16.valueOf(value);
    }

    /**
     * Convert a {@code long} in range 0-4294967295 to a Uint32.
     *
     * @param value value
     * @return Uint32 object
     * @throws IllegalArgumentException if value is less than zero or greater than 4294967295
     */
    public static Uint32 fromJava(final long value) {
        return Uint32.valueOf(value);
    }

    /**
     * Convert a {@link BigInteger} in range 0-18446744073709551615 to an Uint64.
     *
     * @param value value
     * @return Uint64 object
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is less than zero or greater than 18446744073709551615
     */
    public static Uint64 fromJava(final BigInteger value) {
        return Uint64.valueOf(value);
    }

    /**
     * Convert an {@link UnsignedInteger} to a Uint32.
     *
     * @param value value
     * @return Uint32 object
     * @throws NullPointerException if value is null
     */
    public static Uint32 fromGuava(final UnsignedInteger value) {
        return Uint32.valueOf(value);
    }

    /**
     * Convert an {@link UnsignedLong} to a Uint64.
     *
     * @param value value
     * @return Uint64 object
     * @throws NullPointerException if value is null
     */
    public static Uint64 fromGuava(final UnsignedLong value) {
        return Uint64.valueOf(value);
    }

    static void checkNonNegative(final byte value, final String maxValue) {
        if (value < 0) {
            throwIAE(value, maxValue);
        }
    }

    static void checkNonNegative(final short value, final String maxStr) {
        if (value < 0) {
            throwIAE(value, maxStr);
        }
    }

    static void checkNonNegative(final int value, final String maxStr) {
        if (value < 0) {
            throwIAE(value, maxStr);
        }
    }

    static void checkRange(final short value, final short max) {
        if (value < 0 || value > max) {
            throwIAE(value, max);
        }
    }

    static void checkRange(final int value, final int max) {
        if (value < 0 || value > max) {
            throwIAE(value, max);
        }
    }

    static void checkRange(final long value, final long max) {
        if (value < 0 || value > max) {
            throwIAE(value, max);
        }
    }

    private static void throwIAE(final long value, final long max) {
        // "Invalid range: 65536, expected: [[0..65535]]."
        throw new IllegalArgumentException("Invalid range: " + value + ", expected: [[0.." + max + "]].");
    }

    private static void throwIAE(final int value, final String max) {
        // "Invalid range: 65536, expected: [[0..65535]]."
        throw new IllegalArgumentException("Invalid range: " + value + ", expected: [[0.." + max + "]].");
    }
}
