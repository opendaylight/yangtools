/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.DataInput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
final class PersistentBuildInfo extends BuildInfo {
    private final DataInput input;

    private @Nullable BuildConfiguration config;
    private @Nullable ImmutableList<HashedFile> inputFiles;

    PersistentBuildInfo(final DataInput input) {
        this.input = requireNonNull(input);
    }

    @Override
    BuildConfiguration buildConfiguration() throws IOException {
        BuildConfiguration local = config;
        if (local == null) {
            config = local = BuildConfiguration.readFrom(input);
        }
        return local;
    }

    @Override
    ImmutableList<HashedFile> inputFiles() throws IOException {
        ImmutableList<HashedFile> local = inputFiles;
        if (local == null) {
            inputFiles = local = loadInputFiles();
        }
        return local;
    }

    private ImmutableList<HashedFile> loadInputFiles() throws IOException {
        final int inputSize = input.readInt();
        final ImmutableList.Builder<HashedFile> builder = ImmutableList.builderWithExpectedSize(inputSize);
        for (int i = 0; i < inputSize; ++i) {
            builder.add(HashedFile.readFrom(input));
        }

        return builder.build();
    }

    @Override
    ImmutableBuildInfo toImmutable() throws IOException {
        return new ImmutableBuildInfo(buildConfiguration(), inputFiles());
    }
}
