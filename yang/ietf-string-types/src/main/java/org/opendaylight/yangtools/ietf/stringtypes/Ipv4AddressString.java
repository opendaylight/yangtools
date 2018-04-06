/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedStringSupport;

@NonNullByDefault
public class Ipv4AddressString extends AbstractNetworkUnsigned32String<Ipv4AddressString> {
    private static final long serialVersionUID = 1L;

    protected Ipv4AddressString(final int networkByteOrder) {
        super(networkByteOrder);
    }

    public static Ipv4AddressString valueOf(final int networkByteOrder) {
        return new Ipv4AddressString(networkByteOrder);
    }

    public final Inet4Address toJava() {
        try {
            return (Inet4Address) Inet4Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    public final Ipv4AddressString decrement() {
        checkArgument(toInt() != 0, "Decrementing %s would wrap", this);
        return valueOf(toInt() - 1);
    }

    public final Ipv4AddressString increment() {
        checkArgument(toInt() != -1, "Incrementing %s would wrap", this);
        return valueOf(toInt() + 1);
    }

    public final Ipv4AddressString and(final Ipv4AddressString other) {
        return valueOf(toInt() & other.toInt());
    }

    public final Ipv4AddressString or(final Ipv4AddressString other) {
        return valueOf(toInt() | other.toInt());
    }

    public final Ipv4AddressString xor(final Ipv4AddressString other) {
        return valueOf(toInt() ^ other.toInt());
    }

    @Override
    public final String toCanonicalString() {
        return Byte.toUnsignedInt(first())
                + "." + Byte.toUnsignedInt(second())
                + "." + Byte.toUnsignedInt(third())
                + "." + Byte.toUnsignedInt(fourth());
    }

    @Override
    public final DerivedStringSupport<Ipv4AddressString> support() {
        return Ipv4AddressStringSupport.getInstance();
    }
}
