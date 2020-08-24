/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import com.google.common.annotations.Beta;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class IOSupport {
    private static final int MAGICK = 0xAF57BA07;

    private IOSupport() {
        // Hidden on purpose
    }

    public static void writeStatement(final DataOutput out, final IRStatement statement) throws IOException {
        out.writeInt(MAGICK);
        out.writeByte(1);
        new StatementOutputV1(out).writeStatement(statement);
    }

    public static @NonNull IRStatement readStatement(final DataInput in) throws IOException {
        final int magic = in.readInt();
        if (magic != MAGICK) {
            throw new IOException("Unexpected magic " + Integer.toHexString(magic));
        }

        final int version = in.readUnsignedByte();
        final var input = switch (version) {
            case 1 -> new StatementInputV1(in);
            default -> throw new IOException("Unsupported version " + version);
        };

        return input.readStatement();
    }
}
