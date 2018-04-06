/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.eighth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.fifth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.first;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.fourth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.second;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.seventh;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.sixth;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.third;

import com.google.common.annotations.Beta;
import java.util.UUID;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
@ThreadSafe
public class UuidString extends DerivedString<UuidString> {
    private static final long serialVersionUID = 1L;

    private final long mostBits;
    private final long leastBits;

    protected UuidString(final long mostBits, final long leastBits) {
        this.mostBits = mostBits;
        this.leastBits = leastBits;
    }

    protected UuidString(final UuidString other) {
        this(other.mostBits, other.leastBits);
    }

    public final long getMostBits() {
        return mostBits;
    }

    public final long getLeastBits() {
        return leastBits;
    }

    public static UuidString valueOf(final long mostBits, final long leastBits) {
        return new UuidString(mostBits, leastBits);
    }

    public static UuidString valueOf(final byte[] bytes) {
        checkArgument(bytes.length == 16, "Byte array %s has incorrect length", bytes);
        return valueOf(bytes[0] << 56 | bytes[1] << 48 | bytes[2] << 40 | bytes[3] << 32 | bytes[4] << 24
            | bytes[5] << 16 | bytes[6] << 8 | bytes[7], bytes[8] << 56 | bytes[9] << 48 | bytes[10] << 40
            | bytes[11] << 32 | bytes[12] << 24 | bytes[13] << 16 | bytes[14] << 8 | bytes[15]);
    }

    public static UuidString valueOf(final UUID uuid) {
        return valueOf(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public final byte[] toByteArray() {
        return new byte[] {
                first(mostBits), second(mostBits), third(mostBits), fourth(mostBits), fifth(mostBits), sixth(mostBits),
                seventh(mostBits), eighth(mostBits), first(leastBits), second(leastBits), third(leastBits),
                fourth(leastBits), fifth(leastBits), sixth(leastBits), seventh(leastBits), eighth(leastBits)
        };
    }

    public final UUID toUUID() {
        return new UUID(mostBits, leastBits);
    }

    @Override
    public final String toCanonicalString() {
        return toUUID().toString();
    }

    @Override
    public final UuidStringSupport support() {
        return UuidStringSupport.getInstance();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final UuidString o) {
        final int cmp = Long.compareUnsigned(mostBits, o.mostBits);
        return cmp != 0 ? cmp : Long.compareUnsigned(leastBits, leastBits);
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(mostBits) ^ Long.hashCode(leastBits);
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ipv6AddressString)) {
            return false;
        }

        final UuidString other = (UuidString) obj;
        return mostBits == other.mostBits && leastBits == other.leastBits;
    }
}
