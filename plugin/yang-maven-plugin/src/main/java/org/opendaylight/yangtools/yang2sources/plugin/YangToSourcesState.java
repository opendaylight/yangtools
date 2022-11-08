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
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.concepts.WritableObjects;

/**
 * State of the result of a {@link YangToSourcesMojo} execution run.
 */
record YangToSourcesState(
        @NonNull ImmutableMap<String, FileState> inputFiles,
        @NonNull ImmutableMap<String, FileGeneratorArg> configurations,
        @NonNull ImmutableMap<String, FileState> outputFiles) implements WritableObject {

    private static final boolean WITH_PREFIX = true;

    YangToSourcesState {
        requireNonNull(inputFiles);
        requireNonNull(configurations);
        requireNonNull(outputFiles);
    }

    static @NonNull YangToSourcesState readFrom(final DataInput in) throws IOException {
        return new YangToSourcesState(readFileStates(in), readConfigurations(in), readFileStates(in));
    }

    static YangToSourcesState empty() {
        return new YangToSourcesState(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        writeFileStates(out, inputFiles.values());
        writeConfigurations(out, configurations.values());
        writeFileStates(out, outputFiles.values());
    }

    private static void writeConfigurations(final DataOutput out, final Collection<FileGeneratorArg> configurations)
            throws IOException {
        out.writeInt(configurations.size());
        for (var arg : configurations) {
            arg.writeTo(out);
        }
    }

    private static @NonNull ImmutableMap<String, FileGeneratorArg> readConfigurations(final DataInput in)
            throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return ImmutableMap.of();
        }
        final var configurations = ImmutableMap.<String, FileGeneratorArg>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            final var arg = FileGeneratorArg.readFrom(in);
            configurations.put(arg.getIdentifier(), arg);
        }
        return configurations.build();
    }

    private static void writeFileStates(final DataOutput out, final Collection<FileState> items) throws IOException {
        out.writeInt(items.size());
        if (items.isEmpty()) {
            return;
        }

        final String prefix = findPathPrefix(items);
        out.writeUTF(prefix);
        final int cutIndex = prefix.length();
        for (var item : items) {
            out.writeUTF(cutIndex == 0 ? item.path() : item.path().substring(cutIndex));
            WritableObjects.writeLong(out, item.size());
            out.writeInt(item.crc32());
        }
    }

    private static @NonNull ImmutableMap<String, FileState> readFileStates(final DataInput in) throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return ImmutableMap.of();
        }
        final var prefix = in.readUTF();
        final var states = ImmutableMap.<String,FileState>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            final var path = prefix + in.readUTF();
            states.put(path, new FileState(path, WritableObjects.readLong(in), in.readInt()));
        }
        return states.build();
    }

    private static String findPathPrefix(final Collection<FileState> fileStates) {
        String prefix = null;
        for (final var fileState : fileStates) {
            prefix = findCommonPrefix(prefix, fileState.path());
            if (prefix.isEmpty()) {
                break;
            }
        }
        return prefix;
    }

    private static String findCommonPrefix(final String prefix, final String path) {
        if (prefix == null) {
            // first element
            return path;
        }
        for (int i = 0; i < Math.min(path.length(), prefix.length()); i++) {
            if (prefix.charAt(i) != path.charAt(i)) {
                return prefix.substring(0, i);
            }
        }
        return prefix;
    }
}
