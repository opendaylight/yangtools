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
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * Exception thrown when a the specified schema source is not available.
 */
@Beta
public class MissingSchemaSourceException extends SchemaSourceException {
    private static final long serialVersionUID = 1L;

    private final @NonNull SourceIdentifier sourceId;

    public MissingSchemaSourceException(final String message, final SourceIdentifier sourceId) {
        this(message, sourceId, null);
    }

    public MissingSchemaSourceException(final String message, final SourceIdentifier sourceId, final Throwable cause) {
        super(requireNonNull(message), cause);
        this.sourceId = requireNonNull(sourceId);
    }

    public final @NonNull SourceIdentifier sourceId() {
        return sourceId;
    }
}
