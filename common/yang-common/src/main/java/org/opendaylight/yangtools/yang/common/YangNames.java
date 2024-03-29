/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.base.CharMatcher;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for handling various naming conventions mentioned in YANG and related specifications.
 */
public final class YangNames {
    /**
     * A {@link CharMatcher} matching the first character of a YANG {@code identifier} ABNF production,
     * {@code (ALPHA / "_")}.
     */
    public static final @NonNull CharMatcher IDENTIFIER_START =
        CharMatcher.inRange('A', 'Z').or(CharMatcher.inRange('a', 'z').or(CharMatcher.is('_'))).precomputed();
    /**
     * A {@link CharMatcher} NOT matching second and later characters of a YANG {@code identifier} ABNF production,
     * {@code (ALPHA / DIGIT / "_" / "-" / ".")}.
     */
    public static final @NonNull CharMatcher NOT_IDENTIFIER_PART =
        IDENTIFIER_START.or(CharMatcher.inRange('0', '9')).or(CharMatcher.anyOf("-.")).negate().precomputed();

    private YangNames() {
        // Hidden on purpose
    }

    /**
     * Parse a file name according to rules outlined in https://www.rfc-editor.org/rfc/rfc6020#section-5.2. Input string
     * should be the base path with file extension stripped.
     *
     * @param baseName file base name
     * @return A tuple containing the module name and parsed revision, if present.
     * @throws NullPointerException if {@code baseName} is null
     */
    public static @NonNull Entry<@NonNull String, @Nullable String> parseFilename(final String baseName) {
        final int zavinac = baseName.lastIndexOf('@');
        if (zavinac < 0) {
            return new SimpleEntry<>(baseName, null);
        }

        return new SimpleEntry<>(baseName.substring(0, zavinac), baseName.substring(zavinac + 1));
    }
}
