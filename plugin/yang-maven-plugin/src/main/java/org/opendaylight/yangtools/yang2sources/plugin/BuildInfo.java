/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * Build information used to compare inputs that went it a particular build with what is available in the next build.
 */
final class BuildInfo implements Immutable, WritableObject {
    final ImmutableMap<String, InputFile> inputFiles;
    final ImmutableSet<File> outputFiles;

    private BuildInfo(final Map<String, InputFile> inputFiles, final Set<File> outputFiles) {
        this.inputFiles = ImmutableMap.copyOf(inputFiles);
        this.outputFiles = ImmutableSet.copyOf(outputFiles);
    }

    static BuildInfo of(final Collection<File> inputFiles, final Set<File> outputFiles) {
        return new BuildInfo(Maps.uniqueIndex(Collections2.transform(inputFiles, input -> {
            try {
                return InputFile.of(input);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to process file " + input, e);
            }
        }), input -> input.fileName), outputFiles);
    }

    static BuildInfo readFrom(final DataInput in) throws IOException {
        final int inputSize = in.readInt();
        final ImmutableMap.Builder<String, InputFile> inputBuilder = ImmutableMap.builderWithExpectedSize(inputSize);
        for (int i = 0; i < inputSize; ++i) {
            inputBuilder.put(in.readUTF(), InputFile.readFrom(in));
        }

        final int outputSize = in.readInt();
        final ImmutableSet.Builder<File> outputBuilder = ImmutableSet.builderWithExpectedSize(outputSize);
        for (int i = 0; i < inputSize; ++i) {
            outputBuilder.add(new File(in.readUTF()));
        }

        return new BuildInfo(inputBuilder.build(), outputBuilder.build());

    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(inputFiles.size());
        for (Entry<String, InputFile> entry : inputFiles.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeTo(out);
        }
        out.writeInt(outputFiles.size());
        for (File file : outputFiles) {
            out.writeUTF(file.toString());
        }
    }
}
