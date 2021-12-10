/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

public class LeafRefDataValidationFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int errorsCount;

    public LeafRefDataValidationFailedException(final String message, final int errorsCount) {
        super(message);
        this.errorsCount = errorsCount;
    }

    public LeafRefDataValidationFailedException(final String message) {
        this(message, 1);
    }

    public LeafRefDataValidationFailedException(final String message, final Throwable cause) {
        super(message, cause);
        errorsCount = 1;
    }

    public int getValidationsErrorsCount() {
        return errorsCount;
    }
}
