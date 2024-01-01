/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

/**
 * Exception thrown when a failure to acquire a schema source occurs.
 */
@Beta
public class SchemaSourceException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull SourceIdentifier sourceId;

    public SchemaSourceException(final SourceIdentifier sourceId, final String message) {
        super(message);
        this.sourceId = requireNonNull(sourceId);
    }

    public SchemaSourceException(final SourceIdentifier sourceId, final String message, final Throwable cause) {
        super(message, cause);
        this.sourceId = requireNonNull(sourceId);
    }

    public final @NonNull SourceIdentifier sourceId() {
        return sourceId;
    }
}
