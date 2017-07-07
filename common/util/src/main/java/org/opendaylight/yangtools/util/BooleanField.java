/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Utility class for storing an optional boolean in a single byte value. This cuts down the memory requirement quite
 * at very small computational cost.
 *
 * <p>
 * Note: fields do not have to be explicitly initialized, as default initialization value for 'byte', 0, is used to
 *       represent 'not present' condition.
 *
 * @author Robert Varga
 */
@Beta
public final class BooleanField {
    private static final byte ABSENT = 0;
    private static final byte FALSE = -1;
    private static final byte TRUE = 1;

    private BooleanField() {
        throw new UnsupportedOperationException();
    }

    /**
     * Check if a field value has been set, just like {@link Optional#isPresent()}.
     *
     * @param value field value
     * @return True if the value is set.
     * @throws IllegalArgumentException if value is invalid
     */
    public static boolean isPresent(final byte value) {
        switch (value) {
            case ABSENT:
                return false;
            case FALSE:
            case TRUE:
                return true;
            default:
                throw invalidValue(value);
        }
    }

    /**
     * Decode boolean from a field value, just like {@link Optional#get()}.
     *
     * @param value Field value
     * @return Decoded boolean.
     * @throws IllegalArgumentException if value is invalid
     * @throws IllegalStateException if value has not been set
     */
    public static boolean get(final byte value) {
        switch (value) {
            case ABSENT:
                throw new IllegalStateException("Field has not been initialized");
            case FALSE:
                return false;
            case TRUE:
                return true;
            default:
                throw invalidValue(value);
        }
    }

    /**
     * Encode a boolean to a field value, just like {@link Optional#of(Object)}.
     *
     * @param bool Boolean value.
     * @return Field value.
     */
    public static byte of(final boolean bool) {
        return bool ? TRUE : FALSE;
    }

    /**
     * Convert a nullable {@link Boolean} into a field value, just like {@link Optional#ofNullable(Object)}.
     *
     * @param bool Boolean value.
     * @return Field value.
     */
    public static byte ofNullable(final @Nullable Boolean bool) {
        return bool == null ? ABSENT : of(bool.booleanValue());
    }

    /**
     * Convert a field value to a nullable {@link Boolean}. Similar to {@code Optional.orElse(null)}.
     *
     * @param value Fied value.
     * @return Nullable Boolean.
     */
    public static @Nullable Boolean toNullable(final byte value) {
        switch (value) {
            case ABSENT:
                return null;
            case FALSE:
                return Boolean.FALSE;
            case TRUE:
                return Boolean.TRUE;
            default:
                throw invalidValue(value);
        }
    }

    /**
     * Convert a field value into an {@link Optional} {@link Boolean}.
     *
     * @param value Field value.
     * @return Optional {@link Boolean}.
     * @throws IllegalArgumentException if value is invalid.
     */
    public static Optional<Boolean> toOptional(final byte value) {
        switch (value) {
            case ABSENT:
                return Optional.empty();
            case FALSE:
                return Optional.of(Boolean.FALSE);
            case TRUE:
                return Optional.of(Boolean.TRUE);
            default:
                throw invalidValue(value);
        }
    }

    /**
     * Convert a field value into a String representation.
     *
     * @param value Field value.
     * @return Boolean-compatible string, or "absent".
     * @throws IllegalArgumentException if value is invalid.
     */
    public static String toString(final byte value) {
        switch (value) {
            case ABSENT:
                return "absent";
            case FALSE:
                return Boolean.toString(false);
            case TRUE:
                return Boolean.toString(true);
            default:
                throw invalidValue(value);
        }
    }

    private static IllegalArgumentException invalidValue(final byte value) {
        throw new IllegalArgumentException("Invalid field value " + value);
    }
}
