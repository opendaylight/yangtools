/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public interface ListStatement extends MultipleElementsDeclaredStatement,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<QName>,
        ConfigStatementAwareDeclaredStatement<QName>, ActionStatementAwareDeclaredStatement<QName>,
        MustStatementAwareDeclaredStatement<QName>, NotificationStatementAwareDeclaredStatement<QName> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.LIST;
    }

    default @Nullable KeyStatement getKey() {
        final Optional<KeyStatement> opt = findFirstDeclaredSubstatement(KeyStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    default @NonNull Collection<? extends UniqueStatement> getUnique() {
        return declaredSubstatements(UniqueStatement.class);
    }
}
