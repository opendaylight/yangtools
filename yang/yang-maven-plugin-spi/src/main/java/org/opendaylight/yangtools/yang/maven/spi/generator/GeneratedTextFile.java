/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.opendaylight.yangtools.concepts.Delegator;

@Beta
public abstract class GeneratedTextFile extends GeneratedFile implements Delegator<CharSource> {
    public GeneratedTextFile(final GeneratedFileLifecycle lifecycle) {
        super(lifecycle);
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
        return getDelegate().asByteSource(StandardCharsets.UTF_8);
    }
}
