/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import com.google.common.annotations.Beta;

/**
 * SchemaValidationFailedException is thrown when an attempt is made to modify the data tree and the modification
 * does not match the schema context.
 */
@Beta
public class SchemaValidationFailedException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public SchemaValidationFailedException(final String message) {
        super(message);
    }

    public SchemaValidationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
