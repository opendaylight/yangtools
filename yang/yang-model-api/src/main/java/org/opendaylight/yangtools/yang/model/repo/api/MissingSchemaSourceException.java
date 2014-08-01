/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when a the specified schema source is not available.
 */
@Beta
public class MissingSchemaSourceException extends SchemaSourceException {
    private static final long serialVersionUID = 1L;

    public MissingSchemaSourceException(final String message) {
        super(message);
    }

    public MissingSchemaSourceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
