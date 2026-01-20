/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Represents YANG {@code action} statement.
 *
 * <p>The "action" statement is used to define an operation connected to a specific container or list data node. It
 * takes one argument, which is an identifier, followed by a block of substatements that holds detailed action
 * information. The argument is the name of the action.
 */
@NonNullByDefault
public non-sealed interface ActionStatement extends OperationDeclaredStatement {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.ofYang("action", "name", ActionStatement.class,
        ActionEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
