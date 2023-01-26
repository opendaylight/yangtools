/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.concepts.WritableObjects;

/**
 * Hash of a single file state. {@link #size()} corresponds to {@link BasicFileAttributes#size()}.
 */
record FileState(@NonNull String path, long size, int crc32) implements WritableObject {
    FileState {
        requireNonNull(path);
    }

    public static FileState read(final DataInput in) throws IOException {
        return new FileState(in.readUTF(), WritableObjects.readLong(in), in.readInt());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(path);
        WritableObjects.writeLong(out, size);
        out.writeInt(crc32);
    }
}
