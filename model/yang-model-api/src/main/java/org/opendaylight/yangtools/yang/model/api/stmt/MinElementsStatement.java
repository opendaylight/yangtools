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
 * Declared representation of a {@code min-elements} statement.
 */
public interface MinElementsStatement extends DeclaredStatement<MinElementsArgument> {
    /**
     * The definition of {@code min-elements} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        MinElementsStatement.class, MinElementsEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "min-elements", "value");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
