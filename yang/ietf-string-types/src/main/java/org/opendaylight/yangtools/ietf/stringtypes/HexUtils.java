/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class HexUtils {
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

    private HexUtils() {

    }

    static StringBuilder appendHexByte(final StringBuilder sb, final byte b) {
        final int v = Byte.toUnsignedInt(b);
        return sb.append(HEX_CHARS[v >>> 4]).append(HEX_CHARS[v &  15]);
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
}
