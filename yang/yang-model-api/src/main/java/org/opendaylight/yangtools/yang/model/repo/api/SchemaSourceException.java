/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when a failure to acquire a schema source occurs.
 */
@Beta
public class SchemaSourceException extends Exception {
    private static final long serialVersionUID = 1L;

    public SchemaSourceException(final String message) {
        super(message);
    }

    public SchemaSourceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
