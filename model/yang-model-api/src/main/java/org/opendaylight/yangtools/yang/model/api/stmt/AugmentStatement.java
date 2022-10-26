/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement.WithStatus;

/**
 * Declared representation of a {@code augment} statement.
 */
public interface AugmentStatement extends WithStatus<SchemaNodeIdentifier>,
        DataDefinitionAwareDeclaredStatement<SchemaNodeIdentifier>,
        NotificationStatementAwareDeclaredStatement<SchemaNodeIdentifier>,
        ActionStatementAwareDeclaredStatement<SchemaNodeIdentifier>,
        WhenStatementAwareDeclaredStatement<SchemaNodeIdentifier> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.AUGMENT;
    }

    // FIXME: 7.0.0: determine the utility of this method
    default @NonNull Collection<? extends CaseStatement> getCases() {
        return declaredSubstatements(CaseStatement.class);
    }
}
