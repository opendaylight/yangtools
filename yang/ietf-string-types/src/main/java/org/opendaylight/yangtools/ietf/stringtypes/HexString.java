/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import java.util.Arrays;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
@ThreadSafe
public class HexString extends DerivedString<HexString> implements ByteArrayLike {
    private static final long serialVersionUID = 1L;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final HexString EMPTY = new HexString(EMPTY_BYTES);

    private final byte[] bytes;

    protected HexString(final byte[] bytes) {
        this.bytes = bytes.length == 0 ? EMPTY_BYTES : bytes;
    }

    public static HexString empty() {
        return empty();
    }

    public static HexString valueOf(final byte[] bytes) {
        return bytes.length == 0 ? EMPTY : new HexString(bytes.clone());
    }

    @Override
    public byte[] toByteArray() {
        return bytes;
    }

    @Override
    public final int compareTo(final HexString o) {
        for (int i = 0; i < bytes.length; ++i) {
            if (i < o.bytes.length) {
                int cmp = Byte.compare(bytes[i], o.bytes[i]);
                if (cmp != 0) {
                    return cmp;
                }
            } else {
                return 1;
            }
        }

        return bytes.length - o.bytes.length;
    }

    @Override
    public final String toCanonicalString() {
        if (bytes.length == 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder(bytes.length * 3 - 1);
        StringTypeUtils.appendHexByte(sb, bytes[0]);
        for (int i = 1; i < bytes.length; ++i) {
            StringTypeUtils.appendHexByte(sb.append(':'), bytes[i]);
        }
        return sb.toString();
    }

    @Override
    public final HexStringSupport support() {
        return HexStringSupport.getInstance();
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof HexString && Arrays.equals(bytes, ((HexString) obj).bytes);
    }
}
