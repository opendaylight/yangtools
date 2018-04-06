/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    public static Ipv4AddressString valueOf(final byte[] bytes) {
        return valueOf(Ints.fromByteArray(bytes));
    }

    public static Ipv4AddressString valueOf(final Inet4Address address) {
        return valueOf(address.getAddress());
    }

    public final byte[] toByteArray() {
        return Ints.toByteArray(intBits());
    }

    public final Inet4Address toJava() {
        try {
            return (Inet4Address) Inet4Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    public final Ipv4AddressString decrement() {
        checkArgument(intBits() != 0, "Decrementing %s would wrap", this);
        return valueOf(intBits() - 1);
    }

    public final Ipv4AddressString increment() {
        checkArgument(intBits() != -1, "Incrementing %s would wrap", this);
        return valueOf(intBits() + 1);
    }

    public final Ipv4AddressString and(final Ipv4AddressString other) {
        return valueOf(intBits() & other.intBits());
    }

    public final Ipv4AddressString or(final Ipv4AddressString other) {
        return valueOf(intBits() | other.intBits());
    }

    public final Ipv4AddressString xor(final Ipv4AddressString other) {
        return valueOf(intBits() ^ other.intBits());
    }

    @Override
    public final String toCanonicalString() {
        return ByteUtils.appendIpv4Address(new StringBuilder(15), intBits()).toString();
    }

    @Override
    public final DerivedStringSupport<Ipv4AddressString> support() {
        return Ipv4AddressStringSupport.getInstance();
    }

    @Override
    public final int compareTo(final Ipv4AddressString o) {
        return Integer.compareUnsigned(intBits(), o.intBits());
    }

    @Override
    public final int hashCode() {
        return intBits();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof Ipv4AddressString && intBits() == ((Ipv4AddressString) obj).intBits();
    }
}
