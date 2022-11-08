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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.concepts.WritableObjects;

/**
 * State of the result of a {@link YangToSourcesMojo} execution run.
 */
record YangToSourcesState(
        @NonNull ImmutableMap<String, ResourceState> inputFiles,
        @NonNull ImmutableMap<String, ResourceState> configurations,
        @NonNull ImmutableMap<String, ResourceState> outputFiles) implements WritableObject {

    private static final boolean WITH_PREFIX = true;
    private static final boolean WITHOUT_PREFIX = false;

    YangToSourcesState {
        requireNonNull(inputFiles);
        requireNonNull(configurations);
        requireNonNull(outputFiles);
    }

    static @NonNull YangToSourcesState readFrom(final DataInput in) throws IOException {
        final ImmutableMap<String, ResourceState> inputFiles = readStates(in, WITH_PREFIX);
        final ImmutableMap<String, ResourceState> configurations = readStates(in, WITHOUT_PREFIX);
        final ImmutableMap<String, ResourceState> outputFiles = readStates(in, WITH_PREFIX);
        return new YangToSourcesState(inputFiles, configurations, outputFiles);
    }

    static YangToSourcesState empty() {
        return new YangToSourcesState(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        writeStates(out, inputFiles.values(), WITH_PREFIX);
        writeStates(out, configurations.values(), WITHOUT_PREFIX);
        writeStates(out, outputFiles.values(), WITH_PREFIX);
    }

    private static void writeStates(final DataOutput out, final Collection<ResourceState> items,
            final boolean withPrefix) throws IOException {
        out.writeInt(items.size());
        if (items.isEmpty()) {
            return;
        }
        final int cutIndex;
        if (withPrefix) {
            final String prefix = findPathPrefix(items);
            out.writeUTF(prefix);
            cutIndex = prefix.length();
        } else {
            cutIndex = 0;
        }
        for (var item : items) {
            out.writeUTF(cutIndex == 0 ? item.identifier() : item.identifier().substring(cutIndex));
            WritableObjects.writeLong(out, item.size());
            out.writeInt(item.crc32());
        }
    }

    private static ImmutableMap<String, ResourceState> readStates(final DataInput in, final boolean withPrefix)
            throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return ImmutableMap.of();
        }
        final var outputFiles = new ArrayList<ResourceState>(size);
        final var prefix = withPrefix ? in.readUTF() : "";
        for (int i = 0; i < size; ++i) {
            outputFiles.add(new ResourceState(prefix + in.readUTF(), WritableObjects.readLong(in), in.readInt()));
        }
        return Maps.uniqueIndex(outputFiles, ResourceState::identifier);
    }

    private static String findPathPrefix(Collection<ResourceState> fileStates) {
        String prefix = null;
        for (final var fileState : fileStates) {
            prefix = findCommonPrefix(prefix, fileState.identifier());
            if (prefix.isEmpty()) {
                break;
            }
        }
        return prefix;
    }

    private static String findCommonPrefix(String prefix, final String path) {
        if (prefix == null) {
            return path; // first element
        }
        for (int i = 0; i < Math.min(path.length(), prefix.length()); i++) {
            if (prefix.charAt(i) != path.charAt(i)) {
                return prefix.substring(0, i);
            }
        }
        return prefix;
    }
}
