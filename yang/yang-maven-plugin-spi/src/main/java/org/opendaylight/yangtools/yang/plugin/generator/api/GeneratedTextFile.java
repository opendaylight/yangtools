/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The contents of a generated file and its {@link GeneratedFileLifecycle}. This class is suitable for text files,
 * for binary files see {@link GeneratedFile}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class GeneratedTextFile extends GeneratedFile {
    protected GeneratedTextFile(final GeneratedFileLifecycle lifecycle) {
        super(lifecycle);
    }

    @Override
    public final void writeBody(final OutputStream output) throws IOException {
        try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            writeBody(writer);
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
