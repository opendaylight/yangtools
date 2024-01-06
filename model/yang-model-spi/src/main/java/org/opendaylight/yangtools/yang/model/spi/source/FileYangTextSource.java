/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link YangTextSource} backed by a file.
 */
@NonNullByDefault
public final class FileYangTextSource extends YangTextSource implements Delegator<Path> {
    private final Path path;
    private final Charset charset;

    /**
     * Default constructor.
     *
     * @param path Backing path
     * @param charset {@link Charset} to use
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format or if the supplied File is not a file
     * @throws NullPointerException if any argument is {@code null}
     */
    public FileYangTextSource(final Path path, final Charset charset) {
        // FIXME: do not use '.toFile()' here
        super(SourceIdentifier.ofYangFileName(path.toFile().getName()));
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(path + " is not a regular file");
        }
        this.path = path;
        this.charset = requireNonNull(charset);
    }

    public FileYangTextSource(final Path path) {
        this(path, StandardCharsets.UTF_8);
    }

    @Override
    public Path getDelegate() {
        return path;
    }

    @Override
    public Reader openStream() throws IOException {
        return new InputStreamReader(Files.newInputStream(path), charset);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("path", path);
    }

    @Override
    public @NonNull String symbolicName() {
        // FIXME: NEXT: this is forcing internal normalization. I think this boils down to providing Path back, which
        //        is essentially getDelegate() anyway. Perhaps expose it as PathAware?
        return path.toString();
    }
}
