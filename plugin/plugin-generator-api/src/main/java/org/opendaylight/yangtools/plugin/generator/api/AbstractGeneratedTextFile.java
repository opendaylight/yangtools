/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The contents of a generated file and its {@link GeneratedFileLifecycle}. This class is suitable for text files,
 * for binary files see {@link AbstractGeneratedFile}. Text files are encoded in {@link StandardCharsets#UTF_8}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public abstract class AbstractGeneratedTextFile extends AbstractGeneratedFile {
    protected AbstractGeneratedTextFile(final GeneratedFileLifecycle lifecycle) {
        super(lifecycle);
    }

    @Override
    public final void writeBody(final OutputStream output) throws IOException {
        try (var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            try (var buffered = new BufferedWriter(writer)) {
                writeBody(buffered);
            }
        }
    }

    /**
     * Write the body of this file into specified {@link Writer}.
     *
     * @param output writer where to write the output
     * @throws IOException when the stream reports an IOException
     */
    protected abstract void writeBody(Writer output) throws IOException;
}
