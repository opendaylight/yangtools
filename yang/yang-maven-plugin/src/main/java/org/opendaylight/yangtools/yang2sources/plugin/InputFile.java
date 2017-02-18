/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;

final class InputFile implements Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    final String fileName;
    final HashCode hash;

    private InputFile(final String fileName, final HashCode hash) {
        this.fileName = requireNonNull(fileName);
        this.hash = requireNonNull(hash);
    }

    static InputFile of(final File file) throws IOException {
        return new InputFile(file.getPath(), Files.asByteSource(file).hash(Hashing.sha512()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, hash);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InputFile)) {
            return false;
        }
        final InputFile other = (InputFile) obj;
        return fileName.equals(other.fileName) && hash.equals(other.hash);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("fileName", "fileName").add("hash", hash).toString();
    }
}