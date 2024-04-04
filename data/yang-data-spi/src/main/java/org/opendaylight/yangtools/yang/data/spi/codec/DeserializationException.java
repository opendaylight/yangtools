/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

public class DeserializationException extends Exception {
    private static final long serialVersionUID = 1L;

    public DeserializationException() {
    }

    public DeserializationException(final String message) {
        super(message);
    }

    public DeserializationException(final Throwable cause) {
        super(cause);
    }

    public DeserializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
