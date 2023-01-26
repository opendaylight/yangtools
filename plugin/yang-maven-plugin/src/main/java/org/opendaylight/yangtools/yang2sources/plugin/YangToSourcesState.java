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
import java.util.Collection;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;

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
        final ImmutableMap<String, FileState> outputStateMap = readToMap(in, FileState::read, FileState::path);
        return new YangToSourcesState(outputStateMap);

    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        write(out, outputFiles.values());
    }

    private static <T extends WritableObject> void write(final DataOutput out, final Collection<T> items)
            throws IOException {
        out.writeInt(items.size());
        for (var item : items) {
            // TODO: discover common prefix and serialize it just once -- but that will complicate things a log, as
            //       we really maintain a hierarchy, which means we want the Map sorted in a certain way.
            item.writeTo(out);
        }
    }

    private static <T extends WritableObject> ImmutableMap<String, T> readToMap(final DataInput in,
            final DataReader<T> reader, final Function<T, String> keyExtractor) throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            ImmutableMap.of();
        }
        final var outputFiles = new ArrayList<T>(size);
        for (int i = 0; i < size; ++i) {
            outputFiles.add(reader.read(in));
        }
        return Maps.uniqueIndex(outputFiles, keyExtractor::apply);
    }

    @FunctionalInterface
    interface DataReader<T extends WritableObject> {
        T read(DataInput in) throws IOException;
    }
}
