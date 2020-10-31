/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

abstract class HashedFile implements Immutable, WritableObject {
    private static final class FromFile extends HashedFile {
        private final File file;

        FromFile(final File file, final int hash) {
            super(hash);
            this.file = requireNonNull(file);
        }

        @Override
        String fileName() {
            return file.toString();
        }

        @Override
        boolean equalsImpl(final HashedFile other) {
            return other instanceof FromFile ? file.equals(((FromFile) other).file) : super.equalsImpl(other);
        }
    }

    private static final class FromString extends HashedFile {
        private final @NonNull String fileName;

        FromString(final String fileName, final int hash) {
            super(hash);
            this.fileName = requireNonNull(fileName);
        }

        @Override
        String fileName() {
            return fileName;
        }
    }

    static final HashFunction HASH_FUNCTION = Hashing.crc32c();
    static final String HASH_FUNCTION_NAME = "CRC32C";

    private final int hash;

    private HashedFile(final int hash) {
        this.hash = hash;
    }

    static @NonNull HashedFile of(final File file) throws IOException {
        return new FromFile(file, Files.asByteSource(file).hash(HASH_FUNCTION).asInt());
    }

    static @NonNull HashedFile readFrom(final DataInput in) throws IOException {
        return new FromString(in.readUTF(), in.readInt());
    }

    final int hash() {
        return hash;
    }

    abstract @NonNull String fileName();

    @Override
    public final void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(fileName());
        out.writeInt(hash);
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof HashedFile)) {
            return false;
        }
        final HashedFile other = (HashedFile) obj;
        return hash == other.hash && equalsImpl(other);
    }

    boolean equalsImpl(final HashedFile other) {
        return fileName().equals(other.fileName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileName", "fileName")
            .add("hash", hash)
            .toString();
    }
}
