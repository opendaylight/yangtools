/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.DerivedString;

@Beta
@NonNullByDefault
@ThreadSafe
public abstract class AbstractIpv4Address<T extends AbstractIpv4Address<T>> extends DerivedString<T> {
    private static final long serialVersionUID = 1L;

    private final int intBits;

    AbstractIpv4Address(final int intBits) {
        this.intBits = intBits;
    }

    AbstractIpv4Address(final AbstractIpv4Address<?> other) {
        this(other.intBits);
    }

    final int getIntBits() {
        return intBits;
    }

    final int compareBits(final AbstractIpv4Address<?> o) {
        return Integer.compareUnsigned(intBits, o.intBits);
    }

    final boolean equalsBits(final AbstractIpv4Address<?> other) {
        return intBits == other.intBits;
    }
}
