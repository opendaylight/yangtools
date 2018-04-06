/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Longs;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
public abstract class AbstractNetworkUnsigned64String<T extends AbstractNetworkUnsigned64String<T>>
        extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final long longBits;

    protected AbstractNetworkUnsigned64String(final long longBits) {
        this.longBits = longBits;
    }

    public final byte[] toByteArray() {
        return Longs.toByteArray(longBits);
    }

    public final long toLong() {
        return longBits;
    }

    @Override
    public final int compareTo(final T o) {
        return Long.compareUnsigned(longBits, o.toLong());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(longBits);
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractNetworkUnsigned64String)) {
            return false;
        }
        final AbstractNetworkUnsigned64String<?> other = (AbstractNetworkUnsigned64String<?>) obj;
        return longBits == other.longBits
                && other.support().getRepresentationClass().equals(support().getRepresentationClass());
    }

    protected final byte first() {
        return (byte) (longBits >>> 56);
    }

    protected final byte second() {
        return (byte) (longBits >>> 48);
    }

    protected final byte third() {
        return (byte) (longBits >>> 40);
    }

    protected final byte fourth() {
        return (byte) (longBits >>> 32);
    }

    protected final byte fifth() {
        return (byte) (longBits >>> 24);
    }

    protected final byte sixth() {
        return (byte) (longBits >>> 16);
    }

    protected final byte seventh() {
        return (byte) (longBits >>> 8);
    }

    protected final byte eigth() {
        return (byte) longBits;
    }
}
