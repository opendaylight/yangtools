/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.opendaylight.yangtools.ietf.stringtypes.ByteUtils.appendHexByte;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
@ThreadSafe
public class MacAddressString extends AbstractNetworkUnsigned64String<MacAddressString> {
    private static final long serialVersionUID = 1L;

    protected MacAddressString(final long longBits) {
        super(longBits);
    }

    public final byte[] toByteArray() {
        return new byte[] { first(), second(), third(), fourth(), fifth(), sixth() };
    }

    @Override
    public final MacAddressStringSupport support() {
        return MacAddressStringSupport.getInstance();
    }

    @Override
    public final String toCanonicalString() {
        final StringBuilder sb = new StringBuilder(17);
        return appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(
            appendHexByte(sb, third()).append(':'), fourth()).append(':'), fifth()).append(':'), sixth()).append(':'),
            seventh()).append(':'), eighth()).toString();
    }

    @Override
    public final int compareTo(final MacAddressString o) {
        return Long.compareUnsigned(longBits(), o.longBits());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(longBits());
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof MacAddressString && longBits() == ((MacAddressString) obj).longBits();
    }
}
