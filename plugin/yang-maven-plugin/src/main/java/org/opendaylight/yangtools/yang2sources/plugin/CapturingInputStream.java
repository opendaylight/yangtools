/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An {@link InputStream} which captures the sum of its contents.
 */
final class CapturingInputStream extends FilterInputStream {
    private long size;

    CapturingInputStream(final InputStream in) {
        super(new HashingInputStream(Hashing.crc32c(), in));
    }

    @Override
    public int read() throws IOException {
        final int ret = super.read();
        if (ret != -1) {
            size++;
        }
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int ret = super.read(b, off, len);
        if (ret != -1) {
            size += ret;
        }
        return ret;
    }

    @NonNull FileState toFileState(final @NonNull String path) {
        return new FileState(path, size, ((HashingInputStream) in).hash().asInt());
    }
}