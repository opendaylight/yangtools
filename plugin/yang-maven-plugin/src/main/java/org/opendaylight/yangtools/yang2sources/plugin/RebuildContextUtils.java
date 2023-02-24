/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.hash.HashingOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;

/**
 * Utility class serving RebuildContext functionality.
 */
final class RebuildContextUtils {
    private RebuildContextUtils() {
        // utility class
    }

    static FileState buildState(final String identifier, final WritableObject writable) {
        try (var stateStream = new CapturingOutputStream(OutputStream.nullOutputStream());
             var dataStream = new DataOutputStream(stateStream)) {
            writable.writeTo(dataStream);
            return stateStream.buildState(identifier);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static FileState buildState(final File file) {
        try (var cis = new CapturingInputStream(new FileInputStream(file))) {
            cis.readAllBytes();
            return cis.buildState(file.getPath());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static FileState writeContentCaptureState(final GeneratedFile file, final File target) throws IOException {
        try (var cos = new CapturingOutputStream(new FileOutputStream(target))) {
            file.writeBody(cos);
            return cos.buildState(target.getPath());
        }
    }

    static boolean isSame(final Map<String, FileState> oldState, final Map<String, FileState> newState) {
        if (oldState == null || newState == null || oldState.size() != newState.size()) {
            return false;
        }
        for (var entry : oldState.entrySet()) {
            if (!Objects.equals(entry.getValue(), newState.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    static boolean isSameSetOfFiles(final Collection<FileState> states) {
        for (FileState state : states) {
            if (!Files.isRegularFile(Path.of(state.path()))) {
                return false;
            }
        }
        for (FileState state : states) {
            if (!state.equals(buildState(new File(state.path())))) {
                return false;
            }
        }
        return true;
    }

    private static class CapturingOutputStream extends FilterOutputStream {
        private long size;

        CapturingOutputStream(final OutputStream out) {
            super(new HashingOutputStream(Hashing.crc32c(), out));
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public void write(final int b) throws IOException {
            super.write(b);
            size++;
        }

        @Override
        public void write(final byte[] bytes, final int off, final int len) throws IOException {
            super.write(bytes, off, len);
            size += len;
        }

        FileState buildState(final String identifier) {
            return new FileState(identifier, size, ((HashingOutputStream) out).hash().asInt());
        }
    }

    private static class CapturingInputStream extends FilterInputStream {
        private long size;

        CapturingInputStream(final InputStream in) {
            super(new HashingInputStream(Hashing.crc32c(), in));
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int read(final byte[] b) throws IOException {
            size += b.length;
            return super.read(b);
        }

        @Override
        public int read(final byte[] bytes, final int off, final int len) throws IOException {
            size += len;
            return super.read(bytes, off, len);
        }

        FileState buildState(final @NonNull String path) {
            return new FileState(path, size, ((HashingInputStream) in).hash().asInt());
        }
    }
}
