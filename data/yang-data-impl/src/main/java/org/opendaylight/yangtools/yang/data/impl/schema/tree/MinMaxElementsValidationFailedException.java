/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.tree.RequiredElementCountException;

/**
 * Exception thrown when unique constraints would be violated and we cannot throw a
 * {@link RequiredElementCountException}.
 */
final class MinMaxElementsValidationFailedException extends SchemaValidationFailedException {
    private static final long serialVersionUID = 1L;

    MinMaxElementsValidationFailedException(final String message) {
        super(message);
    }
}
