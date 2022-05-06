/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class Tag {

    private static final Pattern TAG_VALIDATION_PATTERN = Pattern.compile("^["
            + "\\x20-\\uDFFF"
            + "\\uE000-\\uFDCF"
            + "\\uFDF0-\\uFFFD"
            + "\\x{10000}-\\x{1FFFD}"
            + "\\x{20000}-\\x{2FFFD}"
            + "\\x{30000}-\\x{3FFFD}"
            + "\\x{40000}-\\x{4FFFD}"
            + "\\x{50000}-\\x{5FFFD}"
            + "\\x{60000}-\\x{6FFFD}"
            + "\\x{70000}-\\x{7FFFD}"
            + "\\x{80000}-\\x{8FFFD}"
            + "\\x{90000}-\\x{9FFFD}"
            + "\\x{A0000}-\\x{AFFFD}"
            + "\\x{B0000}-\\x{BFFFD}"
            + "\\x{C0000}-\\x{CFFFD}"
            + "\\x{D0000}-\\x{DFFFD}"
            + "\\x{E0000}-\\x{EFFFD}"
            + "\\x{F0000}-\\x{FFFFD}"
            + "\\x{100000}-\\x{10FFFD}"
            + "]+$");

    private final TagPrefix prefix;

    private final String value;

    public Tag(final TagPrefix prefix, final String value) {
        this.prefix = prefix;
        this.value = value;
    }

    public static @NonNull Tag create(final TagPrefix prefix, final String value) {
        return new Tag(prefix, value);
    }

    public static @NonNull Tag create(final String prefix, final String value) {
        return new Tag(new TagPrefix(prefix), value);
    }

    public static @NonNull Tag create(final String value) {
        return new Tag(null, value);
    }

    public static @NonNull Tag valueOf(final @NonNull String str) {
        if (isValidTag(str)) {
            final int separatorIdx = str.indexOf(':');
            if (isIetfTag(str)) {
                return create(TagPrefix.IETF, str.substring(separatorIdx + 1));
            } else if (isVendorTag(str)) {
                return create(TagPrefix.VENDOR, str.substring(separatorIdx + 1));
            } else if (isUserTag(str)) {
                return create(TagPrefix.USER, str.substring(separatorIdx + 1));
            } else {
                if (separatorIdx == -1) {
                    return create(str);
                }
                return create(str.substring(0, separatorIdx + 1), str.substring(separatorIdx + 1));
            }
        }
        throw new IllegalArgumentException("Invalid tag value '" + str + "'.");
    }

    private static boolean isValidTag(final String str) {
        return TAG_VALIDATION_PATTERN.matcher(str).matches();
    }

    private static boolean isIetfTag(final String str) {
        return str.startsWith(TagPrefix.IETF.getPrefix());
    }

    private static boolean isVendorTag(final String str) {
        return str.startsWith(TagPrefix.VENDOR.getPrefix());
    }

    private static boolean isUserTag(final String str) {
        return str.startsWith(TagPrefix.USER.getPrefix());
    }

    @Override
    public String toString() {
        return (this.prefix != null ? this.prefix : "") + this.value;
    }

    @VisibleForTesting
    public TagPrefix getPrefix() {
        return this.prefix;
    }

    @VisibleForTesting
    public String getValue() {
        return this.value;
    }
}
