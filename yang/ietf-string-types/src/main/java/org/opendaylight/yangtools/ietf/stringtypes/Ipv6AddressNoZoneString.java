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
import java.net.Inet6Address;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
@ThreadSafe
public class Ipv6AddressNoZoneString extends Ipv6AddressString {
    private static final long serialVersionUID = 1L;

    protected Ipv6AddressNoZoneString(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3) {
        super(intBits0, intBits1, intBits2, intBits3);
    }

    protected Ipv6AddressNoZoneString(final AbstractIpv6Address<?> other) {
        super(other);
    }

    protected Ipv6AddressNoZoneString(final Ipv6AddressString other) {
        super(other);
        checkArgument(!other.getZone().isPresent());
    }

    public static Ipv6AddressNoZoneString valueOf(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3) {
        return new Ipv6AddressNoZoneString(intBits0, intBits1, intBits2, intBits3);
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

    static Ipv6AddressNoZoneString valueOf(final AbstractIpv6Address<?> address) {
        return new Ipv6AddressNoZoneString(address);
    }

    @Override
    public final Optional<String> getZone() {
        return Optional.empty();
    }

    @Override
    public Ipv6AddressNoZoneStringValidator validator() {
        return Ipv6AddressNoZoneStringValidator.getInstance();
    }
}