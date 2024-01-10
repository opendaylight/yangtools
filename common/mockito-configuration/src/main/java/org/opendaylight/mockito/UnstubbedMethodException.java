/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mockito;

import static java.util.Objects.requireNonNull;

/**
 * Exception to be thrown on unstubbed method call.
 */
public final class UnstubbedMethodException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param message detail message
     */
    public UnstubbedMethodException(final String message) {
        super(requireNonNull(message));
    }
}
