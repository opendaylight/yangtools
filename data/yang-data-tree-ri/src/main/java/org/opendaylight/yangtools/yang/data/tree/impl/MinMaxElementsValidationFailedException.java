/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.data.tree.api.RequiredElementCountException;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;

/**
 * Exception thrown when unique constraints would be violated and we cannot throw a
 * {@link RequiredElementCountException}.
 */
final class MinMaxElementsValidationFailedException extends SchemaValidationFailedException
        implements YangNetconfErrorAware {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    // FIXME: 8.0.0: we do not have a path here. Should we just allow DataValidationFailedException whereever our call
    //               sites are?
    MinMaxElementsValidationFailedException(final RequiredElementCountException cause) {
        super(cause.getMessage(), cause);
    }

    @Override
    public RequiredElementCountException getCause() {
        return (RequiredElementCountException) verifyNotNull(super.getCause());
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return getCause().getNetconfErrors();
    }
}
