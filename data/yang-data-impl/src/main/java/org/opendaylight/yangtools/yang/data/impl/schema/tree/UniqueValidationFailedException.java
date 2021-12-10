/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.List;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.data.tree.api.UniqueConstraintException;

/**
 * Exception thrown when unique constraints would be violated and we cannot throw a {@link UniqueConstraintException}.
 */
final class UniqueValidationFailedException extends SchemaValidationFailedException implements YangNetconfErrorAware {
    private static final long serialVersionUID = 1L;

    UniqueValidationFailedException(final String message) {
        super(message);
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.OPERATION_FAILED)
            .appTag("data-not-unique")
            // FIXME: 8.0.0: we are missing path information which should be filled in here. Constructor call site needs
            //               to provide that.
            .build());
    }
}
