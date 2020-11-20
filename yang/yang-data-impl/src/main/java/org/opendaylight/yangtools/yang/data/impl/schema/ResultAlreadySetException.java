/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ResultAlreadySetException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private final transient NormalizedNode resultData;

    public ResultAlreadySetException(final String message, final NormalizedNode resultData) {
        this(message, resultData, null);
    }

    public ResultAlreadySetException(final String message, final NormalizedNode resultData,
            final Throwable cause) {
        super(message, cause);
        this.resultData = resultData;
    }

    public NormalizedNode getResultData() {
        return resultData;
    }
}
