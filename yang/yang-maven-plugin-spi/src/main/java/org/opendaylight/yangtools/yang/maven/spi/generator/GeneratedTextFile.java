/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Beta
public abstract class GeneratedTextFile extends GeneratedFile {
    public GeneratedTextFile(final GeneratedFileLifecycle lifecycle) {
        super(lifecycle);
    }

    @Override
    public void writeBody(final OutputStream output) throws IOException {
        try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            writeBody(writer);
        }
    }

    protected abstract void writeBody(Writer output) throws IOException;
}
