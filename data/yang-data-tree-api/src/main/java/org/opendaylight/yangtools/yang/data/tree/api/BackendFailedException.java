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
 * Exception thrown when the backend of a {@link DataTreeSnapshotCursor} detects an errors which prevents it from
 * completing the requested operation.
 */
@Beta
public class BackendFailedException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public BackendFailedException(final String message) {
        super(message);
    }

    public BackendFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
