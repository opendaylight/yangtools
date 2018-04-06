/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Byte.toUnsignedInt;

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class StringTypeUtils {


    private static final int HEXTETS_IN_IPV6 = 8;
    private static final int[] MASKS = { 0,
            0x80000000, 0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000, 0xFE000000, 0xFF000000,
            0xFF800000, 0xFFC00000, 0xFFE00000, 0xFFF00000, 0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000,
            0xFFFF8000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFF800, 0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00,
            0xFFFFFF80, 0xFFFFFFC0, 0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE, 0xFFFFFFFF,
    };
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static final byte[] HEX_VALUES;

    static {
        final byte[] b = new byte['f' + 1];
        Arrays.fill(b, (byte)-1);

        for (char c = '0'; c <= '9'; ++c) {
            b[c] = (byte)(c - '0');
        }
        for (char c = 'A'; c <= 'F'; ++c) {
            b[c] = (byte)(c - 'A' + 10);
        }
        for (char c = 'a'; c <= 'f'; ++c) {
            b[c] = (byte)(c - 'a' + 10);
        }

        HEX_VALUES = b;
    }

    private StringTypeUtils() {

    }

    static StringBuilder appendDottedQuad(final int allocSize, final int intBits) {
        return new StringBuilder(allocSize).append(toUnsignedInt(first(intBits))).append('.')
                .append(toUnsignedInt(second(intBits))).append('.')
                .append(toUnsignedInt(third(intBits))).append('.')
                .append(toUnsignedInt(fourth(intBits)));
    }

    static byte hextetsToMask(final int[] hextets) {
        checkArgument(hextets.length == HEXTETS_IN_IPV6, "Illegal hextets %s", hextets);
        byte ret = 0;
        for (int i = 0; i < HEXTETS_IN_IPV6; ++i) {
            if (hextets[i] != -1) {
                ret |= 1 << i;
            }
        }
        return ret;
    }

    static StringBuilder appendHexByte(final StringBuilder sb, final byte b) {
        final int v = Byte.toUnsignedInt(b);
        return sb.append(HEX_CHARS[v >>> 4]).append(HEX_CHARS[v &  15]);
    }

    static int findSingleHash(final String str) {
        final int slash = str.indexOf('/');
        checkArgument(slash != -1, "Value \"%s\" does not contain a slash", str);
        checkArgument(str.indexOf('/', slash + 1) == -1, "Value \"%s\" contains multiple slashes", str);
        return slash;
    }

    static byte hexValue(final char c) {
        byte v;
        try {
            // Performance optimization: access the array and rely on the VM for catching
            // illegal access (which boils down to illegal character, which should never happen.
            v = HEX_VALUES[c];
        } catch (IndexOutOfBoundsException e) {
            v = -1;
        }

        if (v < 0) {
            throw new IllegalArgumentException("Invalid character '" + c + "' encountered");
        }

        return v;
    }

    static int maskBits(final int intBits, final int length) {
        return intBits & MASKS[length];
    }

    static byte first(final short shortBits) {
        return (byte)(shortBits >>> 8);
    }

    static byte first(final int intBits) {
        return (byte) (intBits >>> 24);
    }

    static byte first(final long longBits) {
        return (byte) (longBits >>> 56);
    }

    static byte second(final int intBits) {
        return (byte) (intBits >>> 16);
    }

    static byte second(final short shortBits) {
        return (byte) shortBits;
    }

    static byte second(final long longBits) {
        return (byte) (longBits >>> 48);
    }

    static byte third(final int intBits) {
        return (byte) (intBits >>> 8);
    }

    static byte third(final long longBits) {
        return (byte) (longBits >>> 40);
    }

    static byte fourth(final int intBits) {
        return (byte) intBits;
    }

    static byte fourth(final long longBits) {
        return (byte) (longBits >>> 32);
    }

    static byte fifth(final long longBits) {
        return (byte) (longBits >>> 24);
    }

    static byte sixth(final long longBits) {
        return (byte) (longBits >>> 16);
    }

    static byte seventh(final long longBits) {
        return (byte) (longBits >>> 8);
    }

    static byte eighth(final long longBits) {
        return (byte) longBits;
    }
}
