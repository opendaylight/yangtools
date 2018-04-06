/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.appendHexByte;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.first;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.fourth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.second;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
@ThreadSafe
public class MacAddressString extends DerivedString<MacAddressString> implements ByteArrayLike {
    private static final long serialVersionUID = 1L;

    private final int top;
    private final short bottom;

    MacAddressString(final int top, final short bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    protected MacAddressString(final MacAddressString other) {
        this(other.top, other.bottom);
    }

    @Override
    public final byte[] toByteArray() {
        return new byte[] { first(top), second(top), third(top), fourth(top), first(bottom), second(bottom) };
    }

    @Override
    public final MacAddressStringSupport support() {
        return MacAddressStringSupport.getInstance();
    }

    @Override
    public final String toCanonicalString() {
        final StringBuilder sb = new StringBuilder(17);
        return appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(sb,
            first(top)).append(':'), second(top)).append(':'), third(top)).append(':'), fourth(top)).append(':'),
            first(bottom)).append(':'), second(bottom)).toString();
    }

    @Override
    public final int compareTo(final MacAddressString o) {
        final int cmp = Integer.compareUnsigned(top, o.top);
        return cmp != 0 ? cmp : Integer.compare(Short.toUnsignedInt(bottom), Short.toUnsignedInt(o.bottom));
    }

    @Override
    public final int hashCode() {
        return top * 31 + bottom;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MacAddressString)) {
            return false;
        }
        final MacAddressString other = (MacAddressString) obj;
        return top == other.top && bottom == other.bottom;
    }
}
