/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

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

    protected Ipv6AddressString(final AbstractIpv6Address<?> other) {
        super(other);
    }

    public static Ipv6AddressString valueOf(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3) {
        return new Ipv6AddressNoZoneString(intBits0, intBits1, intBits2, intBits3);
    }

    public static Ipv6AddressString valueOf(final AbstractIpv6Address<?> address) {
        return new Ipv6AddressNoZoneString(address);
    }

    public static Ipv6AddressString valueOf(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3, final String zone) {
        return zone.isEmpty() ? valueOf(intBits0, intBits1, intBits2, intBits3)
                : new WithZone(intBits0, intBits1, intBits2, intBits3, zone);
    }

    public static Ipv6AddressString valueOf(final byte[] bytes) {
        checkArgument(bytes.length == 16, "Byte array %s has incorrect length", bytes);
        return valueOf(
            bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3],
            bytes[4] << 24 | bytes[5] << 16 | bytes[6] << 8 | bytes[7],
            bytes[8] << 24 | bytes[9] << 16 | bytes[10] << 8 | bytes[11],
            bytes[12] << 24 | bytes[13] << 16 | bytes[14] << 8 | bytes[15]);
    }

    public static Ipv6AddressString valueOf(final Inet6Address address) {
        return valueOf(address.getAddress());
    }

    public abstract Optional<String> getZone();

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
