/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static java.lang.Byte.toUnsignedInt;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.first;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.fourth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.second;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
@ThreadSafe
public abstract class AbstractIpv4Address<T extends AbstractIpv4Address<T>> extends DerivedString<T>
        implements ByteArrayLike, InetAddressLike {
    private static final long serialVersionUID = 1L;

    private final int intBits;

    AbstractIpv4Address(final int intBits) {
        this.intBits = intBits;
    }

    AbstractIpv4Address(final AbstractIpv4Address<?> other) {
        this(other.intBits);
    }

    @Override
    public final byte getByteAt(final int offset) {
        switch (offset) {
            case 0:
                return first(intBits);
            case 1:
                return second(intBits);
            case 2:
                return third(intBits);
            case 3:
                return fourth(intBits);
            default:
                throw new IndexOutOfBoundsException("Invalid offset " + offset);
        }
    }

    @Override
    public final int getLength() {
        return 4;
    }

    @Override
    public final byte[] toByteArray() {
        return Ints.toByteArray(getIntBits());
    }

    @Override
    public final Inet4Address toInetAddress() {
        try {
            return (Inet4Address) Inet4Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    static StringBuilder toStringBuilder(final int allocSize, final int intBits) {
        return new StringBuilder(allocSize).append(toUnsignedInt(first(intBits))).append('.')
                .append(toUnsignedInt(second(intBits))).append('.').append(toUnsignedInt(third(intBits))).append('.')
                .append(toUnsignedInt(fourth(intBits)));
    }

    final int getIntBits() {
        return intBits;
    }

    final int compareBits(final AbstractIpv4Address<?> other) {
        return Integer.compareUnsigned(intBits, other.intBits);
    }

    final boolean equalsBits(final AbstractIpv4Address<?> other) {
        return intBits == other.intBits;
    }
}
