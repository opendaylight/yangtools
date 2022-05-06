/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public final class Tag implements Immutable {
    private static final Pattern TAG_VALIDATION_PATTERN = Pattern.compile("[\\S ]+");

    private final @NonNull TagPrefix prefix;
    private final @NonNull String value;

    public Tag(final TagPrefix prefix, final String value) {
        this.prefix = requireNonNull(prefix);
        this.value = requireNonNull(value);
    }

    public static @NonNull Tag create(TagPrefix prefix, String value) {
        return new Tag(prefix, value);
    }

    public static @NonNull Tag create(String prefix, String value) {
        return new Tag(new TagPrefix(prefix), value);
    }

    public static @NonNull Tag create(String value) {
        // FIXME: this does not look quite right
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

    private static boolean isValidTag(String str) {
        return str.matches(TAG_VALIDATION_PATTERN.pattern());
    }

    private static boolean isIetfTag(String str) {
        return str.startsWith(TagPrefix.IETF.prefix());
    }

    private static boolean isVendorTag(String str) {
        return str.startsWith(TagPrefix.VENDOR.prefix());
    }

    private static boolean isUserTag(String str) {
        return str.startsWith(TagPrefix.USER.prefix());
    }

    public @NonNull TagPrefix prefix() {
        return prefix;
    }

    public @NonNull String value() {
        return value;
    }

    @Override
    public String toString() {
        return (prefix != null ? prefix : "") + value;
    }
}
