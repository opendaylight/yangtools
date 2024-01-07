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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link YinTextSource} backed by a file.
 */
@NonNullByDefault
public class FileYinTextSource extends YinTextSource implements Delegator<Path> {
    private final Path path;

    public FileYinTextSource(final SourceIdentifier sourceId, final Path path) {
        super(sourceId);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Supplied path " + path + " is not a regular file");
        }
        this.path = requireNonNull(path);
    }

    public FileYinTextSource(final Path path) {
        // FIXME: do not use toFile() here
        this(SourceIdentifier.ofYinFileName(path.toFile().getName()), path);
    }

    @Override
    public final Path getDelegate() {
        return path;
    }

    @Override
    public final InputStream openStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public final @NonNull String symbolicName() {
        return path.toString();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("path", path);
    }
}
