/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.InetAddress;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@NonNullByDefault
public final class Ipv4AddressStringSupport extends AbstractDerivedStringSupport<Ipv4AddressString> {

    private static final Ipv4AddressStringSupport INSTANCE = new Ipv4AddressStringSupport();

    private Ipv4AddressStringSupport() {
        super(Ipv4AddressString.class);
    }

    public static Ipv4AddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Ipv4AddressString fromString(final String str) {
        final InetAddress address = InetAddresses.forString(str);
        checkArgument(address instanceof Inet4Address, "Value \"%s\" is not a valid ipv4-address", str);
        return fromAddress((Inet4Address) address);
    }

    public Ipv4AddressString fromAddress(final Inet4Address address) {
        return fromByteArray(address.getAddress());
    }

    public Ipv4AddressString fromByteArray(final byte[] bytes) {
        return fromInt(Ints.fromByteArray(bytes));
    }

    public Ipv4AddressString fromInt(final int networkByteOrder) {
        return new Ipv4AddressString(networkByteOrder);
    }
}
