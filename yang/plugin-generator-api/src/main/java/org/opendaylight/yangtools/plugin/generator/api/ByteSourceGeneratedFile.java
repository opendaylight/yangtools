/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A generated file with a body is available as a {@link ByteSource}.
 */
@NonNullByDefault
final class ByteSourceGeneratedFile extends AbstractGeneratedFile {
    private final ByteSource body;

    ByteSourceGeneratedFile(final GeneratedFileLifecycle lifecycle, final ByteSource body) {
        super(lifecycle);
        this.body = requireNonNull(body);
    }

    @Override
    public void writeBody(final @NonNull OutputStream output) throws IOException {
        body.copyTo(output);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("body", body);
    }
}
