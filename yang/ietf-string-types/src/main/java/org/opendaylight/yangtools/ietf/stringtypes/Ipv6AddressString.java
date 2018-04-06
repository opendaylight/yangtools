/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.first;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.fourth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.second;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
@ThreadSafe
public abstract class Ipv6AddressString extends AbstractIpv6Address<Ipv6AddressString> implements ByteArrayLike,
        InetAddressLike {
    public static class WithZone extends Ipv6AddressString {
        private static final Interner<String> ZONE_INTERNER = Interners.newWeakInterner();
        private static final long serialVersionUID = 1L;

        private final String zone;

        protected WithZone(final int intBits0, final int intBits1, final int intBits2, final int intBits3,
                final String zone) {
            super(intBits0, intBits1, intBits2, intBits3);
            this.zone = ZONE_INTERNER.intern(zone);
        }

        protected WithZone(final WithZone other) {
            super(other);
            this.zone = other.zone;
        }

        @Override
        public final Optional<String> getZone() {
            return Optional.of(zone);
        }

        private Object readResolve() {
            return valueOf(getIntBits0(), getIntBits1(), getIntBits2(), getIntBits3(), zone);
        }
    }

    private static final long serialVersionUID = 1L;

    protected Ipv6AddressString(final int intBits0, final int intBits1, final int intBits2, final int intBits3) {
        super(intBits0, intBits1, intBits2, intBits3);
    }

    protected Ipv6AddressString(final Ipv6AddressString other) {
        super(other);
    }

    Ipv6AddressString(final AbstractIpv6Address<?> other) {
        super(other);
    }

    public static Ipv6AddressString valueOf(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3, final String zone) {
        return zone.isEmpty() ? Ipv6AddressNoZoneString.valueOf(intBits0, intBits1, intBits2, intBits3)
                : new WithZone(intBits0, intBits1, intBits2, intBits3, zone);
    }

    public static Ipv6AddressString valueOf(final byte[] address, final String zone) {
        return zone.isEmpty() ? Ipv6AddressNoZoneString.valueOf(address)
                : new WithZone(address[0] << 24 | address[1] << 16 | address[2] << 8 | address[3],
                    address[4] << 24 | address[5] << 16 | address[6] << 8 | address[7],
                    address[8] << 24 | address[9] << 16 | address[10] << 8 | address[11],
                    address[12] << 24 | address[13] << 16 | address[14] << 8 | address[15], zone);
    }

    public static Ipv6AddressString valueOf(final Inet6Address address, final String zone) {
        return valueOf(address.getAddress(), zone);
    }

    public abstract Optional<String> getZone();

    @Override
    public final byte getByteAt(final int offset) {
        switch (offset) {
            case 0:
                return first(getIntBits0());
            case 1:
                return second(getIntBits0());
            case 2:
                return third(getIntBits0());
            case 3:
                return fourth(getIntBits0());
            case 4:
                return first(getIntBits1());
            case 5:
                return second(getIntBits1());
            case 6:
                return third(getIntBits1());
            case 7:
                return fourth(getIntBits1());
            case 8:
                return first(getIntBits2());
            case 9:
                return second(getIntBits2());
            case 10:
                return third(getIntBits2());
            case 11:
                return fourth(getIntBits2());
            case 12:
                return first(getIntBits3());
            case 13:
                return second(getIntBits3());
            case 14:
                return third(getIntBits3());
            case 15:
                return fourth(getIntBits3());
            default:
                throw new IndexOutOfBoundsException("Invalid offset " + offset);
        }
    }

    @Override
    public final int getLength() {
        return 16;
    }

    @Override
    public final byte[] toByteArray() {
        return bitsAsArray();
    }

    @Override
    public final Inet6Address toInetAddress() {
        try {
            return (Inet6Address) Inet6Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    @Override
    public final String toCanonicalString() {
        return hextetsToIPv6String(new StringBuilder(39), createHextets()).toString();
    }

    @Override
    public final Ipv6AddressStringSupport support() {
        return Ipv6AddressStringSupport.getInstance();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Ipv6AddressString o) {
        return compareBits(o);
    }

    @Override
    public final int hashCode() {
        return hashBits();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof Ipv6AddressString && equalsBits((Ipv6AddressString) obj);
    }
}
