/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

/**
 * Exception thrown when a failure to translate a schema source between
 * representations.
 */
public class SchemaSourceTransformationException extends Exception {
    private static final long serialVersionUID = 1L;

    public SchemaSourceTransformationException(final String message) {
        super(message);
    }

    public SchemaSourceTransformationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
