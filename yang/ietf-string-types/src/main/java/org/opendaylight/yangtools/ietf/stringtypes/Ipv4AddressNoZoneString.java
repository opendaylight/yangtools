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
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
@ThreadSafe
public class Ipv4AddressNoZoneString extends Ipv4AddressString {
    private static final long serialVersionUID = 1L;
    private static final Ipv4AddressNoZoneString EMPTY = new Ipv4AddressNoZoneString(0);

    protected Ipv4AddressNoZoneString(final int intBits) {
        super(intBits);
    }

    protected Ipv4AddressNoZoneString(final Ipv4AddressString other) {
        super(other);
        checkArgument(!other.getZone().isPresent());
    }

    @Override
    public final Optional<String> getZone() {
        return Optional.empty();
    }

    public static Ipv4AddressNoZoneString valueOf(final int intBits) {
        return intBits == 0 ? EMPTY : new Ipv4AddressNoZoneString(intBits);
    }

    public static Ipv4AddressNoZoneString valueOf(final byte[] bytes) {
        return valueOf(Ints.fromByteArray(bytes));
    }

    public static Ipv4AddressNoZoneString valueOf(final Inet4Address address) {
        return Ipv4AddressNoZoneString.valueOf(address.getAddress());
    }

    @Override
    public Ipv4AddressNoZoneStringValidator validator() {
        return Ipv4AddressNoZoneStringValidator.getInstance();
    }
}