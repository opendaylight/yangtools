/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.concepts.WritableObjects;

/**
 * State of the result of a {@link YangToSourcesMojo} execution run.
 */
// FIXME: expand to capture:
//        - input YANG files
//        - code generators and their config
record YangToSourcesState(@NonNull ImmutableMap<String, FileState> outputFiles) implements WritableObject {
    YangToSourcesState {
        requireNonNull(outputFiles);
    }

    static @NonNull YangToSourcesState readFrom(final DataInput in) throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return new YangToSourcesState(ImmutableMap.of());
        }

        final var outputFiles = new ArrayList<FileState>(size);
        for (int i = 0; i < size; ++i) {
            outputFiles.add(new FileState(in.readUTF(), WritableObjects.readLong(in), in.readInt()));
        }
        return new YangToSourcesState(Maps.uniqueIndex(outputFiles, FileState::path));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(outputFiles.size());

        for (var entry : outputFiles.entrySet()) {
            // TODO: discover common prefix and serialize it just once -- but that will complicate things a log, as
            //       we really maintain a hierarchy, which means we want the Map sorted in a certain way.
            out.writeUTF(entry.getKey());
            final var state = entry.getValue();
            WritableObjects.writeLong(out, state.size());
            out.writeInt(state.crc32());
        }
    }
}
