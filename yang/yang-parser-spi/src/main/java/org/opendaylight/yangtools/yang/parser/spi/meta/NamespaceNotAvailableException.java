/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;

/**
 *
 * Thrown when identifier namespace is not available (supported)
 * in specific model processing phase.
 *
 */
public class NamespaceNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NamespaceNotAvailableException(String message) {
        super(Preconditions.checkNotNull(message));
    }

    public NamespaceNotAvailableException(String message, Throwable cause) {
        super(Preconditions.checkNotNull(message), cause);
    }
}
