/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code container} statement.
 */
public interface ContainerStatement extends DataDefinitionStatement,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<QName>,
        ActionStatementAwareDeclaredStatement<QName>, ConfigStatementAwareDeclaredStatement<QName>,
        NotificationStatementAwareDeclaredStatement<QName>, MustStatementAwareDeclaredStatement<QName> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.CONTAINER;
    }

    default @Nullable PresenceStatement getPresence() {
        final var opt = findFirstDeclaredSubstatement(PresenceStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
