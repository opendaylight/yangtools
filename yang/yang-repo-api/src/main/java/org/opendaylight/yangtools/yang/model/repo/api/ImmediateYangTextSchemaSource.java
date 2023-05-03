/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link YangTextSchemaSource} with content readily available.
 */
public class ImmediateYangTextSchemaSource extends YangTextSchemaSource {
    private final @Nullable String symbolicName;
    private final byte @NonNull [] bytes;

    public ImmediateYangTextSchemaSource(final SourceIdentifier identifier, final byte[] bytes) {
        this(identifier, bytes, null);
    }

    public ImmediateYangTextSchemaSource(final SourceIdentifier identifier, final byte[] bytes,
            final @Nullable String symbolicName) {
        super(identifier);
        this.bytes = bytes.clone();
        this.symbolicName = symbolicName;
    }

    @Override
    public final Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    @Override
    public final InputStream openStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }
}
