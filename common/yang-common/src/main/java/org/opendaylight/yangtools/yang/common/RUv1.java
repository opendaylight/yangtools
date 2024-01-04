/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;

/**
 * Serialization proxy for {@link RevisionUnion}.
 */
final class RUv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private String dateString;

    @SuppressWarnings("checkstyle:redundantModifier")
    public RUv1() {
        // For Externalizable
    }

    RUv1(final String dateString) {
        this.dateString = requireNonNull(dateString);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final var length = dateString.length();
        out.writeInt(length);
        if (length != 0) {
            out.write(dateString.getBytes(StandardCharsets.US_ASCII));
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        final int length = in.readInt();
        if (length != 0) {
            final var bytes = new byte[length];
            in.readFully(bytes);
            dateString = new String(bytes, StandardCharsets.US_ASCII);
        } else {
            dateString = "";
        }
    }

    @java.io.Serial
    private Object readResolve() {
        return RevisionUnion.of(dateString);
    }
}
