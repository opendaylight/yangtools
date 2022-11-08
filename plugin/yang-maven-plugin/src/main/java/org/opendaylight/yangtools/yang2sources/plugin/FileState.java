/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Hash of a single file state. {@link #size()} corresponds to {@link BasicFileAttributes#size()}.
 */
record FileState(@NonNull String path, long size, int crc32) {
    @FunctionalInterface
    interface FileContent {
        void writeTo(@NonNull OutputStream out) throws IOException ;
    }

    FileState {
        requireNonNull(path);
    }

    static @NonNull FileState ofFile(final File file) throws IOException {
        try (var cis = new CapturingInputStream(new FileInputStream(file))) {
            cis.readAllBytes();
            return new FileState(file.getPath(), cis.size(), cis.crc32c());
        }
    }

    static @NonNull FileState ofWrittenFile(final File file, final FileContent content) throws IOException {
        try (var out = new CapturingOutputStream(Files.newOutputStream(file.toPath()))) {
            content.writeTo(out);
            return new FileState(file.getPath(), out.size(), out.crc32c());
        }
    }
}
