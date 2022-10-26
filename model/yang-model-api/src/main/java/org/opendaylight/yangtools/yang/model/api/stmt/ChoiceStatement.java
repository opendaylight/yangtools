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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code choice} statement.
 */
public interface ChoiceStatement extends DataDefinitionStatement, ConfigStatementAwareDeclaredStatement<QName>,
        DefaultStatementAwareDeclaredStatement, MandatoryStatementAwareDeclaredStatement<QName> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.CHOICE;
    }

    default @NonNull Collection<? extends CaseStatement> getCases() {
        return declaredSubstatements(CaseStatement.class);
    }
}
