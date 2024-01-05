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
import java.nio.charset.StandardCharsets;

/**
 * Serialization proxy for {@link RevisionUnion}.
 */
final class RUv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final int LENGTH = Revision.MAX_VALUE.toString().length();

    private String dateString;

    @SuppressWarnings("checkstyle:redundantModifier")
    public RUv1() {
        // For Externalizable
    }

    RUv1(final String dateString) {
        final int length = dateString.length();
        if (length != 0 && length != LENGTH) {
            throw new IllegalArgumentException("Unexpected string length " + length);
        }
        this.dateString = dateString;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final var length = dateString.length();
        out.writeByte(length);
        if (length != 0) {
            out.write(dateString.getBytes(StandardCharsets.US_ASCII));
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        final int length = in.readByte();
        if (length == LENGTH) {
            final var bytes = new byte[length];
            in.readFully(bytes);
            dateString = new String(bytes, StandardCharsets.US_ASCII);
        } else if (length == 0) {
            dateString = "";
        } else {
            throw new IOException("Unexpected byte length " + length);
        }
    }

    @java.io.Serial
    private Object readResolve() {
        return dateString.isEmpty() ? null : Revision.of(dateString);
    }
}
