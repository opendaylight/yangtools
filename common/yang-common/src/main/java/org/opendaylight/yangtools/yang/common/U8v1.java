/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Serialization proxy for {@link Uint64}.
 */
// FIXME: v2 with protobuf-like varint encoding
final class U8v1 implements Externalizable  {
    @java.io.Serial
    private static final long serialVersionUID = 6883430190287723549L;

    private Long bits;

    @SuppressWarnings("checkstyle:redundantModifier")
    public U8v1() {
        // For Externalizable
    }

    U8v1(final Uint64 value) {
        bits = value.longValue();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeLong(bits);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        bits = in.readLong();
    }

    @java.io.Serial
    private Object readResolve() {
        return Uint64.fromLongBits(bits);
    }
}
