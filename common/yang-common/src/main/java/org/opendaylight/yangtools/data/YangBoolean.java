/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A YANG {@code boolean} value.
 */
@NonNullByDefault
public final class YangBoolean implements Comparable<YangBoolean>, ScalarValue {
    public static final YangBoolean TRUE = new YangBoolean(true);
    public static final YangBoolean FALSE = new YangBoolean(false);

    private final boolean value;

    private YangBoolean(final boolean value) {
        this.value = value;
    }

    /**
     * Returns an {@code YangBoolean} holding the value of the specified {@code String}.
     *
     * @param string String to parse
     * @return A YangBoolean instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if string is neither {@code true} nor {@code false}
     */
    public static YangBoolean valueOf(final String string) {
        return switch (requireNonNull(string)) {
            case "false" -> FALSE;
            case "true" -> TRUE;
            default -> throw new IllegalArgumentException("'" + string + "' is not a valid boolean value");
        };
    }

    public static YangBoolean valueOf(final boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean value() {
        return value;
    }

    @Override
    public int compareTo(final YangBoolean other) {
        return Boolean.compare(value, other.value);
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof YangBoolean other && value == other.value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }
}
