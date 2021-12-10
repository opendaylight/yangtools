/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.data.tree.api.RequiredElementCountException;

/**
 * Exception thrown when unique constraints would be violated and we cannot throw a
 * {@link RequiredElementCountException}.
 */
final class MinMaxElementsValidationFailedException extends SchemaValidationFailedException
        implements YangNetconfErrorAware {
    private static final long serialVersionUID = 1L;

    private final int min;
    private final int max;
    private final int actual;

    // FIXME: 8.0.0: we do not have a path here. Should we just allow DataValidationFailedException whereever our call
    //               sites are?
    MinMaxElementsValidationFailedException(final String message, final int min, final int max, final int actual) {
        super(message);
        this.min = min;
        this.max = max;
        this.actual = actual;
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return new RequiredElementCountException(YangInstanceIdentifier.empty(), actual, min, max, "dummy")
            .getNetconfErrors();
    }
}
