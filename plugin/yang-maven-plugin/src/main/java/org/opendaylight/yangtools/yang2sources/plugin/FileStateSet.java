/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.concepts.WritableObjects;

/**
 * A collection of {@link FileState} objects indexed by their {@link FileState#path()}.
 */
record FileStateSet(@NonNull ImmutableMap<String, FileState> fileStates) implements WritableObject {
    private static final @NonNull FileStateSet EMPTY = new FileStateSet(ImmutableMap.of());

    FileStateSet {
        requireNonNull(fileStates);
    }

    static @NonNull FileStateSet empty() {
        return EMPTY;
    }

    static @NonNull FileStateSet ofNullable(final @Nullable ImmutableMap<String, FileState> fileStates) {
        return fileStates == null || fileStates.isEmpty() ? EMPTY : new FileStateSet(fileStates);
    }

    static @NonNull FileStateSet readFrom(final DataInput in) throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return EMPTY;
        }

        final var prefix = in.readUTF();
        final var fileStates = ImmutableMap.<String, FileState>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            final var path = prefix + in.readUTF();
            fileStates.put(path, new FileState(path, WritableObjects.readLong(in), in.readInt()));
        }
        return new FileStateSet(fileStates.build());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(fileStates.size());
        if (fileStates.isEmpty()) {
            // Nothing else here
            return;
        }

        final var prefix = findPathPrefix(fileStates.keySet());
        out.writeUTF(prefix);

        final int cutIndex = prefix.length();
        for (var fileState : fileStates.values()) {
            final var path = fileState.path();
            out.writeUTF(cutIndex == 0 ? path : path.substring(cutIndex));
            WritableObjects.writeLong(out, fileState.size());
            out.writeInt(fileState.crc32());
        }
    }

    private static String findPathPrefix(final Set<String> filePaths) {
        if (filePaths.size() == 1) {
            // Single item: do not use a prefix
            return "";
        }

        final var it = filePaths.iterator();
        var prefix = it.next();

        do {
            prefix = Strings.commonPrefix(prefix, it.next());
        } while (!prefix.isEmpty() && it.hasNext());

        return prefix;
    }
}
