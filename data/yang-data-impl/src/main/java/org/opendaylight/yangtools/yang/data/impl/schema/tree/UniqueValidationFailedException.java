/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorTag;
import org.opendaylight.yangtools.yang.common.YangError;
import org.opendaylight.yangtools.yang.data.api.schema.tree.UniqueConstraintException;

/**
 * Exception thrown when unique constraints would be violated and we cannot throw a {@link UniqueConstraintException}.
 */
final class UniqueValidationFailedException extends SchemaValidationFailedException implements YangError {
    private static final long serialVersionUID = 1L;

    UniqueValidationFailedException(final String message) {
        super(message);
    }

    @Override
    public ErrorTag getErrorTag() {
        return ErrorTag.OPERATION_FAILED;
    }

    @Override
    public Optional<String> getErrorAppTag() {
        return Optional.of("data-not-unique");
    }
}
