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
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;

/**
 * Utility class serving RebuildContext functionality.
 */
final class RebuildContextUtils {
    private RebuildContextUtils() {
        // utility class
    }

    static ResourceState buildState(final String identifier, final WritableObject writable) {
        try (var cos = new CapturingOutputStream(OutputStream.nullOutputStream());
             var oos = new DataOutputStream(cos)) {
            writable.writeTo(oos);
            return new ResourceState(identifier, cos.size(), cos.crc32c());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static ResourceState buildState(final File file) {
        try (var cos = new CapturingInputStream(new FileInputStream(file))) {
            cos.readAllBytes();
            return new ResourceState(file.getPath(), cos.size(), cos.crc32c());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static ResourceState buildState(final GeneratedFilePath path, final GeneratedFile file) {
        try (var cos = new CapturingOutputStream(OutputStream.nullOutputStream())) {
            file.writeBody(cos);
            return new ResourceState(path.getPath(), cos.size(), cos.crc32c());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean isSame(final Map<String, ResourceState> oldState, final Map<String, ResourceState> newState) {
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

    static boolean isSameFiles(final Collection<ResourceState> states) {
        for (ResourceState state : states) {
            if (!Files.isRegularFile(Path.of(state.identifier()))) {
                return false;
            }
        }
        for (ResourceState state : states) {
            if (!state.equals(buildState(new File(state.identifier())))) {
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

        long size() {
            return size;
        }

        int crc32c() {
            return ((HashingOutputStream) out).hash().asInt();
        }
    }

    private static class CapturingInputStream extends FilterInputStream {
        private long size;

        CapturingInputStream(final InputStream in) {
            super(new HashingInputStream(Hashing.crc32c(), in));
        }

        @SuppressWarnings("checkstyle:parameterName")
        public int read(byte[] b) throws IOException {
            size += b.length;
            return super.read(b);
        }

        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            size += len;
            return super.read(bytes, off, len);
        }

        long size() {
            return size;
        }

        int crc32c() {
            return ((HashingInputStream) in).hash().asInt();
        }
    }
}
