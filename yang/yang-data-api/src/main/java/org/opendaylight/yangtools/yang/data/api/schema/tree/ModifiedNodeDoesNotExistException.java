/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Exception thrown when a proposed change fails validation before being
 * applied into the Data Tree because tree node which child nodes are
 * modified or written did not exist when transaction started
 * and still does not exists when transaction is processed.
 *
 * <p>
 * Note if node existed in first place and was removed by other transaction,
 * thrown exception should be {@link ConflictingModificationAppliedException}.
 */
public class ModifiedNodeDoesNotExistException extends DataValidationFailedException {
    private static final long serialVersionUID = 1L;

    public ModifiedNodeDoesNotExistException(final String messagePattern, final YangInstanceIdentifier path,
            final Throwable cause) {
        super(messagePattern, path, cause);
    }

    public ModifiedNodeDoesNotExistException(final String messagePattern, final YangInstanceIdentifier path) {
        super(messagePattern, path);
    }

    public ModifiedNodeDoesNotExistException(final YangInstanceIdentifier path, final String message,
            final Throwable cause) {
        super(path, message, cause);
    }

    public ModifiedNodeDoesNotExistException(final YangInstanceIdentifier path, final String message) {
        super(path, message);
    }
}
