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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A {@link YangTextSchemaSource} backed by a file.
 */
final class YangTextFileSchemaSource extends YangTextSchemaSource implements Delegator<Path> {
    private final @NonNull Path path;
    private final @NonNull Charset charset;

    YangTextFileSchemaSource(final SourceIdentifier sourceId, final Path path, final Charset charset) {
        super(sourceId);
        this.path = requireNonNull(path);
        this.charset = requireNonNull(charset);
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
    public Optional<String> getSymbolicName() {
        // FIXME: NEXT: this is forcing internal normalization. I think this boils down to providing Path back, which
        //        is essentially getDelegate() anyway. Perhaps expose it as PathAware?
        return Optional.of(path.toString());
    }
}
