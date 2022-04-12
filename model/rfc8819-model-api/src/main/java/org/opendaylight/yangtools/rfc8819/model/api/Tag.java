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
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class Tag {

    private final String prefix;
    private final String tagValue;
    private final String tagType;

    private Tag(final String prefix, final String tagValue, final String tagType) {
        this.prefix = prefix;
        this.tagValue = tagValue;
        this.tagType = tagType;
    }

    public static @NonNull Tag create(String prefix, String tagValue, String tagType) {
        return new Tag(prefix, tagValue, tagType);
    }

    public static @NonNull Tag create(String tagValue, String tagType) {
        return create(null, tagValue, tagType);
    }
    public static @NonNull Tag valueOf(final @NonNull String str) {
        if (isValidTag(str)) {
            if (isIetfTag(str) || isVendorTag(str) || isUserTag(str)) {
                final int separatorIdx = str.indexOf(':');
                return create(str.substring(0, separatorIdx + 1),
                        str.substring(separatorIdx + 1), getTagType(str));
            } else {
                return create(str, ModuleTagTypes.RESERVED.name());
            }
        }
        return create(str, ModuleTagTypes.INVALID.name()); // FIXME: throw an exception, when is tag invalid?
    }

    private static String getTagType(String str) {
        if (isIetfTag(str)) {
            return ModuleTagTypes.IETF.name();
        } else if (isVendorTag(str)) {
            return ModuleTagTypes.VENDOR.name();
        }
        return ModuleTagTypes.USER.name();
     }

    private static boolean isValidTag(String str) {
        return str.matches("[\\S ]+");
    }

    private static boolean isIetfTag(String str) {
        return str.startsWith(ModuleTagTypes.IETF.getPrefix());
    }

    private static boolean isVendorTag(String str) {
        return str.startsWith(ModuleTagTypes.VENDOR.getPrefix());
    }

    private static boolean isUserTag(String str) {
        return str.startsWith(ModuleTagTypes.USER.getPrefix());
    }

    @Override
    public String toString() {
        return (prefix != null ? prefix : "") + tagValue;
    }
    @VisibleForTesting
    public String getPrefix() {
        return prefix;
    }
    @VisibleForTesting
    public String getTagValue() {
        return tagValue;
    }
    @VisibleForTesting
    public String getTagType() {
        return tagType;
    }
}
