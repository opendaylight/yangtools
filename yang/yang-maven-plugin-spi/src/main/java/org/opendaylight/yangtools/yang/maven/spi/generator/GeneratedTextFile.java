/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

@Beta
public final class GeneratedTextFile extends GeneratedFile implements Delegator<CharSource> {
    private final @NonNull CharSource delegate;

    public GeneratedTextFile(final CharSource delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public CharSource getDelegate() {
        return delegate;
    }

    @Override
    public InputStream openStream() throws IOException {
        return asByteSource().openStream();
    }

    @Override
    public InputStream openBufferedStream() throws IOException {
        return asByteSource().openBufferedStream();
    }

    private ByteSource asByteSource() {
        return delegate.asByteSource(StandardCharsets.UTF_8);
    }
}
