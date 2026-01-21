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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code reference} statement.
 */
public interface ReferenceStatement extends DeclaredHumanTextStatement {
    /**
     * The definition of {@code reference} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        ReferenceStatement.class, ReferenceEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "reference", "text", true);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
