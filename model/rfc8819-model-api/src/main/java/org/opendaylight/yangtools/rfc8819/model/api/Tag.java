/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An RFC8819 {@code tag}.
 *
 * @param value the tag value
 */
@Beta
@NonNullByDefault
public record Tag(String value) {
    private static final Interner<Tag> INTERNER = Interners.newWeakInterner();
    private static final Pattern TAG_VALUE_PATTERN = Pattern.compile("""
        ^[\
        \\x20-\\uDFFF\
        \\uE000-\\uFDCF\
        \\uFDF0-\\uFFFD\
        \\x{10000}-\\x{1FFFD}\
        \\x{20000}-\\x{2FFFD}\
        \\x{30000}-\\x{3FFFD}\
        \\x{40000}-\\x{4FFFD}\
        \\x{50000}-\\x{5FFFD}\
        \\x{60000}-\\x{6FFFD}\
        \\x{70000}-\\x{7FFFD}\
        \\x{80000}-\\x{8FFFD}\
        \\x{90000}-\\x{9FFFD}\
        \\x{A0000}-\\x{AFFFD}\
        \\x{B0000}-\\x{BFFFD}\
        \\x{C0000}-\\x{CFFFD}\
        \\x{D0000}-\\x{DFFFD}\
        \\x{E0000}-\\x{EFFFD}\
        \\x{F0000}-\\x{FFFFD}\
        \\x{100000}-\\x{10FFFD}\
        ]+$""");

    /**
     * Default constructor.
     *
     * @param value the tag value does not conform to specification
     * @throws IllegalArgumentException if the
     */
    public Tag {
        requireNonNull(value);
        if (!isValidValue(value)) {
            throw new IllegalArgumentException("Invalid tag value '" + value + "'");
        }
    }

    /**
     * Returns {@code true} if this tag has specific {@link Prefix}.
     *
     * @param prefix the {@link Prefix}
     * @return {@code true} if this tag has specific {@link Prefix}
     */
    public boolean hasPrefix(final Prefix prefix) {
        return value.startsWith(prefix.value());
    }

    /**
     * {@return an interned equivalent of this tag}
     */
    public Tag intern() {
        return INTERNER.intern(this);
    }

    static boolean isValidValue(final String str) {
        return TAG_VALUE_PATTERN.matcher(str).matches();
    }
}
