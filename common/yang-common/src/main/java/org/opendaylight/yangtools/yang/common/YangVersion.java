/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of supported YANG versions.
 */
public enum YangVersion {
    /**
     * Version 1, as defined in RFC6020.
     */
    VERSION_1("1", "RFC6020"),
    /**
     * Version 1.1, as defined in RFC7950.
     */
    VERSION_1_1("1.1", "RFC7950");

    private final @NonNull String str;
    private final @NonNull String reference;

    YangVersion(final @NonNull String str, final @NonNull String reference) {
        this.str = requireNonNull(str);
        this.reference = requireNonNull(reference);
    }

    /**
     * Parse a YANG version from its textual representation.
     *
     * @param str String to parse
     * @return YANG version, or {@code null}
     * @throws NullPointerException if the string is {@code null}
     */
    public static @Nullable YangVersion forString(final @NonNull String str) {
        return switch (requireNonNull(str)) {
            case "1" -> VERSION_1;
            case "1.1" -> VERSION_1_1;
            default -> null;
        };
    }

    /**
     * Parse a YANG version from its textual representation.
     *
     * @param str String to parse
     * @return YANG version
     * @throws NullPointerException if the string is {@code null}
     * @throws IllegalArgumentException if the string is not recognized
     */
    public static @NonNull YangVersion ofString(final @NonNull String str) {
        final var ret = forString(str);
        if (ret != null) {
            return ret;
        }
        throw new IllegalArgumentException("Invalid YANG version " + str);
    }

    /**
     * Parse a YANG version from its textual representation.
     *
     * @param str String to parse
     * @return An Optional YANG version
     * @throws NullPointerException if the string is {@code null}
     * @deprecated Use {@link #forString(String)} or {@link #ofString(String)}
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static Optional<YangVersion> parse(final @NonNull String str) {
        return Optional.ofNullable(forString(str));
    }

    /**
     * Return the normative reference defining this YANG version.
     *
     * @return Normative reference.
     */
    public @NonNull String reference() {
        return reference;
    }

    /**
     * Return the normative reference defining this YANG version.
     *
     * @return Normative reference.
     * @deprecated Use {@link #reference()} instead
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public @NonNull String getReference() {
        return reference;
    }

    @Override
    public @NonNull String toString() {
        return str;
    }
}
