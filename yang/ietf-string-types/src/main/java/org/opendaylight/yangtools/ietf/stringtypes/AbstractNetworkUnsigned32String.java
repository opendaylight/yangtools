/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

@NonNullByDefault
public abstract class AbstractNetworkUnsigned32String<T extends AbstractNetworkUnsigned32String<T>>
        extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final int intBits;

    protected AbstractNetworkUnsigned32String(final int intBits) {
        this.intBits = intBits;
    }

    protected final int intBits() {
        return intBits;
    }

    protected final byte first() {
        return ByteUtils.first(intBits);
    }

    protected final byte second() {
        return ByteUtils.second(intBits);
    }

    protected final byte third() {
        return ByteUtils.third(intBits);
    }

    protected final byte fourth() {
        return ByteUtils.fourth(intBits);
    }
}
