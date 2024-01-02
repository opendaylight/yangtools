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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A {@link YinTextSchemaSource} backed by a file.
 */
final class YinTextFileSchemaSource extends YinTextSchemaSource implements Delegator<Path> {
    private final @NonNull Path path;

    YinTextFileSchemaSource(final @NonNull SourceIdentifier sourceId, final @NonNull Path path) {
        super(sourceId);
        this.path = requireNonNull(path);
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
    public Optional<String> getSymbolicName() {
        return Optional.of(path.toString());
    }
}
