/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;

/**
 * A {@link YinTextSource} backed by a file.
 */
@NonNullByDefault
public final class YinTextFileSource extends YinTextSource implements Delegator<Path> {
    private final Path path;

    public YinTextFileSource(final Path path) {
        // FIXME: do not use '.toFile()' here
        super(SourceIdentifier.ofYinFileName(path.toFile().getName()));
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(path + " is not a regular file");
        }
        this.path = path;
    }

    @Override
    public Path getDelegate() {
        return path;
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("path", path);
    }

    @Override
    public @NonNull String symbolicName() {
        return path.toString();
    }
}
