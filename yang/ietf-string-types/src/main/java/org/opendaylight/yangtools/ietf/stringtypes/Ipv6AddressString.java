/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedStringSupport;

@NonNullByDefault
public class Ipv6AddressString extends AbstractNetworkUnsigned128String<Ipv6AddressString> {
    private static final long serialVersionUID = 1L;

    // FIXME: this needs to be in superclass to hide the footprint
    private transient volatile short zeroRun;

    protected Ipv6AddressString(final long firstLong, final long secondLong) {
        super(firstLong, secondLong);
        zeroRun = -1;
    }

    protected Ipv6AddressString(final Ipv6AddressString from) {
        super(from);
        zeroRun = from.zeroRun;
    }

    public static Ipv6AddressString valueOf(final byte[] bytes) {
        checkArgument(bytes.length == 16, "Byte array %s has incorrect length", bytes);
        return new Ipv6AddressString(bytes[0] << 56 | bytes[1] << 48 | bytes[2] << 40 | bytes[3] << 32
            | bytes[4] << 24 | bytes[5] << 16 | bytes[6] << 8 | bytes[7],
            bytes[8] << 56 | bytes[9] << 48 | bytes[10] << 40 | bytes[11] << 32
            | bytes[12] << 24 | bytes[13] << 16 | bytes[14] << 8 | bytes[15]);
    }

    public static Ipv6AddressString valueOf(final Inet6Address address) {
        return valueOf(address.getAddress());
    }

    public final byte[] toByteArray() {
        return new byte[] { first(), second(), third(), fourth(), fifth(), sixth(), seventh(), eighth(),
                ninth(), tenth(), eleventh(), twelfth(), thirteenth(), fourteenth(), fifteenth(), sixteenth() };
    }

    public final Inet6Address toJava() {
        try {
            return (Inet6Address) Inet6Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    @Override
    public final String toCanonicalString() {
        short local = zeroRun;
        if (local == -1) {
            zeroRun = local = ByteUtils.computeZeroRun(firstLong(), secondLong());
        }

        return ByteUtils.appendIpv6Address(new StringBuilder(), local, firstLong(), secondLong()).toString();
    }

    @Override
    public final DerivedStringSupport<Ipv6AddressString> support() {
        return Ipv6AddressStringSupport.getInstance();
    }

    @Override
    public final int compareTo(final Ipv6AddressString o) {
        final int cmp = Long.compareUnsigned(firstLong(), o.firstLong());
        return cmp != 0 ? cmp : Long.compareUnsigned(secondLong(), o.secondLong());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(firstLong()) * 31 + Long.hashCode(secondLong());
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ipv6AddressString)) {
            return false;
        }

        final Ipv6AddressString other = (Ipv6AddressString) obj;
        return firstLong() == other.firstLong() && secondLong() == other.secondLong();
    }
}
