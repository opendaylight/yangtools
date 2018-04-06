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
public abstract class AbstractNetworkUnsigned64String<T extends AbstractNetworkUnsigned64String<T>>
        extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final long longBits;

    protected AbstractNetworkUnsigned64String(final long longBits) {
        this.longBits = longBits;
    }

    protected AbstractNetworkUnsigned64String(final AbstractNetworkUnsigned64String<?> other) {
        this(other.longBits);
    }

    protected final long longBits() {
        return longBits;
    }

    protected final byte first() {
        return ByteUtils.first(longBits);
    }

    protected final byte second() {
        return ByteUtils.second(longBits);
    }

    protected final byte third() {
        return ByteUtils.third(longBits);
    }

    protected final byte fourth() {
        return ByteUtils.fourth(longBits);
    }

    protected final byte fifth() {
        return ByteUtils.fifth(longBits);
    }

    protected final byte sixth() {
        return ByteUtils.sixth(longBits);
    }

    protected final byte seventh() {
        return ByteUtils.seventh(longBits);
    }

    protected final byte eighth() {
        return ByteUtils.eighth(longBits);
    }
}
