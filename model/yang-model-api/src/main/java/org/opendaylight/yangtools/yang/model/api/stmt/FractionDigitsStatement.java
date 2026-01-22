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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code fraction-digits} statement.
 */
public interface FractionDigitsStatement extends DeclaredStatement<Integer> {
    /**
     * The definition of {@code fraction-digits} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        FractionDigitsStatement.class, FractionDigitsEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "fraction-digits", "value");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
