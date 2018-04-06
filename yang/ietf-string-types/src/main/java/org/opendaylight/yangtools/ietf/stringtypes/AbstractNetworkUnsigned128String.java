/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
public abstract class AbstractNetworkUnsigned128String<T extends AbstractNetworkUnsigned128String<T>>
        extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final long firstLongBits;
    private final long secondLongBits;

    protected AbstractNetworkUnsigned128String(final long firstLongBits, final long secondLongBits) {
        this.firstLongBits = firstLongBits;
        this.secondLongBits = secondLongBits;
    }

    protected AbstractNetworkUnsigned128String(final AbstractNetworkUnsigned128String<?> other) {
        this(other.firstLongBits, other.secondLongBits);
    }

    protected final long firstLong() {
        return firstLongBits;
    }

    protected final long secondLong() {
        return secondLongBits;
    }

    protected final byte first() {
        return ByteUtils.first(firstLongBits);
    }

    protected final byte second() {
        return ByteUtils.second(firstLongBits);
    }

    protected final byte third() {
        return ByteUtils.third(firstLongBits);
    }

    protected final byte fourth() {
        return ByteUtils.fourth(firstLongBits);
    }

    protected final byte fifth() {
        return ByteUtils.fifth(firstLongBits);
    }

    protected final byte sixth() {
        return ByteUtils.sixth(firstLongBits);
    }

    protected final byte seventh() {
        return ByteUtils.seventh(firstLongBits);
    }

    protected final byte eighth() {
        return ByteUtils.eighth(firstLongBits);
    }

    protected final byte ninth() {
        return ByteUtils.first(secondLongBits);
    }

    protected final byte tenth() {
        return ByteUtils.second(secondLongBits);
    }

    protected final byte eleventh() {
        return ByteUtils.third(secondLongBits);
    }

    protected final byte twelfth() {
        return ByteUtils.fourth(secondLongBits);
    }

    protected final byte thirteenth() {
        return ByteUtils.fifth(secondLongBits);
    }

    protected final byte fourteenth() {
        return ByteUtils.sixth(secondLongBits);
    }

    protected final byte fifteenth() {
        return ByteUtils.seventh(secondLongBits);
    }

    protected final byte sixteenth() {
        return ByteUtils.eighth(secondLongBits);
    }
}
