/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * The contents of a generated file and its {@link GeneratedFileLifecycle}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface GeneratedFile extends Immutable {
    /**
     * Return the lifecycle governing this file.
     *
     * @return Governing lifecycle
     */
    GeneratedFileLifecycle getLifecycle();

    /**
     * Write the body of this file into specified {@link OutputStream}.
     *
     * @param output stream where to write the output
     * @throws IOException when the stream reports an IOException
     */
    void writeBody(OutputStream output) throws IOException;

    /**
     * Create a new {@link GeneratedFile} with the specified {@link CharSequence} body. The body will be encoded in
     * {@link StandardCharsets#UTF_8}.
     *
     * @param lifecycle File lifecycle
     * @param body File body
     * @return A GeneratedFile.
     * @throws NullPointerException if any argument is null
     */
    static GeneratedFile of(final GeneratedFileLifecycle lifecycle, final CharSequence body) {
        return new CharSeqGeneratedTextFile(lifecycle, body);
    }

    /**
     * Create a new {@link GeneratedFile} with the specified {@link CharSource} body. The body will be encoded in
     * {@link StandardCharsets#UTF_8}.
     *
     * @param lifecycle File lifecycle
     * @param body File body
     * @return A GeneratedFile.
     * @throws NullPointerException if any argument is null
     */
    static GeneratedFile of(final GeneratedFileLifecycle lifecycle, final CharSource body) {
        return new CharSourceGeneratedTextFile(lifecycle, body);
    }

    /**
     * Create a new {@link GeneratedFile} with the specified {@link ByteSource} body. The body will be written as is.
     *
     * @param lifecycle File lifecycle
     * @param body File body
     * @return A GeneratedFile.
     * @throws NullPointerException if any argument is null
     */
    static GeneratedFile of(final GeneratedFileLifecycle lifecycle, final ByteSource body) {
        return new ByteSourceGeneratedFile(lifecycle, body);
    }
}
