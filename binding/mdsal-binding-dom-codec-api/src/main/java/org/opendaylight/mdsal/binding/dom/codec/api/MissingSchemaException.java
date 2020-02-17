/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;

/**
 * Thrown when codec was used with data which are not modeled and available in schema used by codec.
 */
@Beta
public class MissingSchemaException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public MissingSchemaException(final String msg) {
        super(msg);
    }

    public MissingSchemaException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
