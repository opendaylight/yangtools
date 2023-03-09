/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link StateStorage} based on a single project file.
 */
final class FileStateStorage extends StateStorage {
    private final Path stateFile;

    FileStateStorage(final Path stateFile) {
        this.stateFile = requireNonNull(stateFile);
    }

    @Override
    @Nullable YangToSourcesState loadState() throws IOException {
        if (Files.exists(stateFile)) {
            try (var in = new DataInputStream(Files.newInputStream(stateFile))) {
                return YangToSourcesState.readFrom(in);
            }
        }
        return null;
    }

    @Override
    void storeState(final YangToSourcesState state) throws IOException {
        Files.createDirectories(stateFile.getParent());
        try (var out = new DataOutputStream(Files.newOutputStream(stateFile))) {
            state.writeTo(out);
        }
    }

    @Override
    void deleteState() throws IOException {
        Files.deleteIfExists(stateFile);
    }
}