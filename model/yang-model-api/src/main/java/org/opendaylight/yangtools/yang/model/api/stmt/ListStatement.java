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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code list} statement.
 */
public interface ListStatement extends MultipleElementsDeclaredStatement,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<QName>,
        ConfigStatementAwareDeclaredStatement<QName>, ActionStatementAwareDeclaredStatement<QName>,
        MustStatementAwareDeclaredStatement<QName>, NotificationStatementAwareDeclaredStatement<QName> {
    /**
     * The definition of {@code list} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        ListStatement.class, ListEffectiveStatement.class, YangConstants.RFC6020_YIN_MODULE, "list", "name");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    default @Nullable KeyStatement getKey() {
        final var opt = findFirstDeclaredSubstatement(KeyStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }

    default @NonNull Collection<? extends @NonNull UniqueStatement> getUnique() {
        return declaredSubstatements(UniqueStatement.class);
    }
}
