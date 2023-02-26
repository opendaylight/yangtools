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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A collection of {@link FileState} objects indexed by their {@link FileState#path()}.
 */
record FileStateSet(@NonNull ImmutableMap<String, FileState> fileStates) implements WritableObject {
    FileStateSet {
        requireNonNull(fileStates);
    }

    static @NonNull FileStateSet readFrom(final DataInput in) throws IOException {
        final int size = in.readInt();
        final var fileStates = ImmutableMap.<String, FileState>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            final var fileState = FileState.read(in);
            fileStates.put(fileState.path(), fileState);
        }
        return new FileStateSet(fileStates.build());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(fileStates.size());
        for (var fileState : fileStates.values()) {
            // TODO: discover common prefix and serialize it just once -- but that will complicate things a log, as
            //       we really maintain a hierarchy, which means we want the Map sorted in a certain way.
            fileState.writeTo(out);
        }
    }
}
