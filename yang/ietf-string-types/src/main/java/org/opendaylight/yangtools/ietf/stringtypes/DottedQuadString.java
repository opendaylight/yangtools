/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
@ThreadSafe
public class DottedQuadString extends AbstractIpv4Address<DottedQuadString> implements ByteArrayLike, InetAddressLike {
    private static final long serialVersionUID = 1L;

    protected DottedQuadString(final int intBits) {
        super(intBits);
    }

    protected DottedQuadString(final DottedQuadString other) {
        super(other);
    }

    public static DottedQuadString valueOf(final int intBits) {
        return new DottedQuadString(intBits);
    }

    public static DottedQuadString valueOf(final byte[] bytes) {
        return valueOf(Ints.fromByteArray(bytes));
    }

    public static DottedQuadString valueOf(final Inet4Address address) {
        return valueOf(address.getAddress());
    }

    @Override
    public final byte[] toByteArray() {
        return Ints.toByteArray(getIntBits());
    }

    public final int toIntBits() {
        return getIntBits();
    }

    @Override
    public final Inet4Address toInetAddress() {
        try {
            return (Inet4Address) Inet4Address.getByAddress(toByteArray());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to convert address", e);
        }
    }

    @Override
    public final String toCanonicalString() {
        return StringTypeUtils.appendDottedQuad(15, getIntBits()).toString();
    }

    @Override
    public final DottedQuadStringSupport support() {
        return DottedQuadStringSupport.getInstance();
    }

    @Override
    public final int compareTo(final DottedQuadString o) {
        return compareBits(o);
    }

    @Override
    public final int hashCode() {
        return getIntBits();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof DottedQuadString && equalsBits((DottedQuadString) obj);
    }
}
