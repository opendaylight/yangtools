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

/**
 * State of the result of a {@link YangToSourcesMojo} execution run.
 */
// FIXME: expand to capture:
//        - input YANG files
record YangToSourcesState(
        @NonNull ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs,
        @NonNull FileStateSet outputFiles) implements WritableObject {
    // 'ymp' followed by a byte value '1'
    private static final int MAGIC = 0x796D7001;

    YangToSourcesState {
        requireNonNull(fileGeneratorArgs);
        requireNonNull(outputFiles);
    }

    static @NonNull YangToSourcesState readFrom(final DataInput in) throws IOException {
        final int magic = in.readInt();
        if (magic != MAGIC) {
            throw new IOException("Unrecognized magic " + Integer.toHexString(magic));
        }
        return new YangToSourcesState(readConfigurations(in), FileStateSet.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(MAGIC);
        writeConfigurations(out, fileGeneratorArgs.values());
        outputFiles.writeTo(out);
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
}
