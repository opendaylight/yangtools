/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
@ThreadSafe
public abstract class Ipv4AddressString extends AbstractIpv4Address<Ipv4AddressString> implements ByteArrayLike,
        InetAddressLike {
    public static class WithZone extends Ipv4AddressString {
        private static final Interner<String> ZONE_INTERNER = Interners.newWeakInterner();
        private static final long serialVersionUID = 1L;

        private final String zone;

        protected WithZone(final int intBits, final String zone) {
            super(intBits);
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
            return valueOf(getIntBits(), zone);
        }
    }

    private static final long serialVersionUID = 1L;

    protected Ipv4AddressString(final int intBits) {
        super(intBits);
    }

    protected Ipv4AddressString(final Ipv4AddressString other) {
        super(other);
    }

    public static Ipv4AddressString valueOf(final int intBits, final String zone) {
        return zone.isEmpty() ? Ipv4AddressNoZoneString.valueOf(intBits) : new WithZone(intBits, zone);
    }

    public static Ipv4AddressString valueOf(final byte[] address, final String zone) {
        return valueOf(Ints.fromByteArray(address), zone);
    }

    public static Ipv4AddressString valueOf(final Inet4Address address, final String zone) {
        return valueOf(address.getAddress(), zone);
    }

    public abstract Optional<String> getZone();

    @Override
    public final String toCanonicalString() {
        final Optional<String> optZone = getZone();
        final int addLen = optZone.isPresent() ? optZone.get().length() + 1 : 0;
        final StringBuilder sb = toStringBuilder(15 + addLen, getIntBits());
        if (optZone.isPresent()) {
            sb.append('%').append(optZone.get());
        }
        return sb.toString();
    }

    @Override
    public final Ipv4AddressStringSupport support() {
        return Ipv4AddressStringSupport.getInstance();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Ipv4AddressString o) {
        final int cmp = compareBits(o);
        if (cmp != 0) {
            return cmp;
        }
        final Optional<String> our = getZone();
        final Optional<String> their = o.getZone();
        if (our.isPresent()) {
            return their.isPresent() ? our.get().compareTo(their.get()) : 1;
        }
        return their.isPresent() ? -1 : 0;
    }

    @Override
    public final int hashCode() {
        return getIntBits() ^ getZone().hashCode();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ipv4AddressString)) {
            return false;
        }
        final Ipv4AddressString other = (Ipv4AddressString) obj;
        return equalsBits(other) && getZone().equals(other.getZone());
    }
}
