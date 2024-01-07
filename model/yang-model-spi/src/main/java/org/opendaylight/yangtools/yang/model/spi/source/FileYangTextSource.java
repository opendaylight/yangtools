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
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link YangTextSource} backed by a file.
 */
@NonNullByDefault
public class FileYangTextSource extends AbstractYangTextSource<Path> {
    private final Charset charset;

    /**
     * Default constructor.
     *
     * @param path Backing path
     * @param identifier Source identifier
     * @param charset expected stream character set
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the supplied path is not a regular file
     */
    public FileYangTextSource(final SourceIdentifier sourceId, final Path path, final Charset charset) {
        super(sourceId, path);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Supplied path " + path + " is not a regular file");
        }
        this.charset = requireNonNull(charset);
    }

    /**
     * Utility constructor. Derives the {@link SourceIdentifier} from {@link Path} and assumes UTF-8 encoding.
     *
     * @param path backing path
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public FileYangTextSource(final Path path) {
        // FIXME: do not use .toFile() here
        this(SourceIdentifier.ofYangFileName(path.toFile().getName()), path, StandardCharsets.UTF_8);
    }

    @Override
    public final Reader openStream() throws IOException {
        return new InputStreamReader(Files.newInputStream(getDelegate()), charset);
    }

    @Override
    public final @NonNull String symbolicName() {
        // FIXME: NEXT: this is forcing internal normalization. I think this boils down to providing Path back, which
        //        is essentially getDelegate() anyway. Perhaps expose it as PathAware?
        return getDelegate().toString();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("path", getDelegate());
    }
}
