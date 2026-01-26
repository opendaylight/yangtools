/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code range} statement.
 */
public interface RangeStatement extends ConstrainedDocumentedDeclaredStatement<ValueRanges> {
    /**
     * The definition of {@code range} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<ValueRanges, @NonNull RangeStatement, @NonNull RangeEffectiveStatement> DEF =
        StatementDefinition.of(RangeStatement.class, RangeEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "range", YangArgumentDefinitions.VALUE_AS_VALUE_RANGES);

    @Override
    default StatementDefinition<ValueRanges, ?, ?> statementDefinition() {
        return DEF;
    }
}
