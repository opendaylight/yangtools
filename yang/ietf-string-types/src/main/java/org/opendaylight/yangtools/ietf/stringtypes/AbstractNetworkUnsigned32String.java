/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.primitives.Ints;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

@NonNullByDefault
public abstract class AbstractNetworkUnsigned32String<T extends AbstractNetworkUnsigned32String<T>>
        extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final int intbits;

    protected AbstractNetworkUnsigned32String(final int intBits) {
        this.intbits = intBits;
    }

    public final byte[] toByteArray() {
        return Ints.toByteArray(intbits);
    }

    public final int toInt() {
        return intbits;
    }

    @Override
    public final int compareTo(final T o) {
        return Integer.compareUnsigned(intbits, o.toInt());
    }

    @Override
    public final int hashCode() {
        return intbits;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractNetworkUnsigned32String)) {
            return false;
        }
        final AbstractNetworkUnsigned32String<?> other = (AbstractNetworkUnsigned32String<?>) obj;
        return intbits == other.intbits
                && other.support().getRepresentationClass().equals(support().getRepresentationClass());
    }

    protected final byte first() {
        return (byte) (intbits >>> 24);
    }

    protected final byte second() {
        return (byte) (intbits >>> 16);
    }

    protected final byte third() {
        return (byte) (intbits >>> 8);
    }

    protected final byte fourth() {
        return (byte) intbits;
    }
}
