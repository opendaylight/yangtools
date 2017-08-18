/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

/**
 * Thrown when identifier namespace is not available (supported)
 * in specific model processing phase.
 */
public class NamespaceNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NamespaceNotAvailableException(final String message) {
        super(requireNonNull(message));
    }

    public NamespaceNotAvailableException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
    }
}
