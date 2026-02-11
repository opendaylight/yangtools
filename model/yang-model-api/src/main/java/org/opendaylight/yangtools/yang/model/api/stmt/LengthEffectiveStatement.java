/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.ConstraintCompat;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code length} statement.
 */
public interface LengthEffectiveStatement extends ConstraintCompat<ValueRanges, @NonNull LengthStatement> {
    @Override
    default StatementDefinition<ValueRanges, @NonNull LengthStatement, ?> statementDefinition() {
        return LengthStatement.DEF;
    }

    // FIXME: sharpen to LengthConstraint
    @Override
    ConstraintMetaDefinition asConstraint();
}
