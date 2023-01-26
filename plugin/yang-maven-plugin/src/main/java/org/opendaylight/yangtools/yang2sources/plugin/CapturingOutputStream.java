/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} which captures the sum of its contents.
 */
final class CapturingOutputStream extends FilterOutputStream {
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
