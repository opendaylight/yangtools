/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code grouping} statement.
 */
public interface GroupingStatement extends DocumentedDeclaredStatement.WithStatus<QName>,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<QName>,
        NotificationStatementAwareDeclaredStatement<QName>, ActionStatementAwareDeclaredStatement<QName> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.GROUPING;
    }
}
