/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link YangTextSchemaSource} with content readily available.
 */
public class StringYangTextSchemaSource extends YangTextSchemaSource {
    private final @Nullable String symbolicName;
    private final @NonNull String content;

    public StringYangTextSchemaSource(final SourceIdentifier sourceId, final String content) {
        this(sourceId, content, null);
    }

    public StringYangTextSchemaSource(final SourceIdentifier sourceId, final String content,
            final @Nullable String symbolicName) {
        super(sourceId);
        this.content = requireNonNull(content);
        this.symbolicName = symbolicName;
    }

    @Override
    public final Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    @Override
    public final Reader openStream() {
        return new StringReader(content);
    }
}
